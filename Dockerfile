# syntax=docker/dockerfile:1

# =============================================================================
# Stage 1 — Build the Spring Boot application with Maven on Alpine
# =============================================================================
FROM maven:3.10-eclipse-temurin-25-alpine AS build

WORKDIR /build

# Dependencies are resolved in their own layer so that source changes do not
# invalidate the Maven cache.
COPY pom.xml ./
RUN mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package \
    && mv target/backend-*.jar /build/application.jar

# =============================================================================
# Stage 2 — Prepare the Alpine (musl) JRE and the unprivileged runtime user
# =============================================================================
FROM eclipse-temurin:25-jre-alpine AS jre

RUN addgroup -S -g 10001 kora \
    && adduser -S -D -H -u 10001 -G kora kora \
    && mkdir -p /tmp \
    && chmod 1777 /tmp

# =============================================================================
# Stage 3 — Distroless runtime (no shell, no package manager, no OS tools)
# =============================================================================
FROM scratch AS runtime

ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
    LANG=C.UTF-8 \
    TZ=UTC

# JRE and the native libraries it loads dynamically.
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
COPY --from=jre /lib/ld-musl-x86_64.so.1 /lib/ld-musl-x86_64.so.1
COPY --from=jre /lib/libc.musl-x86_64.so.1 /lib/libc.musl-x86_64.so.1
COPY --from=jre /usr/lib/libz.so.1 /usr/lib/libz.so.1
COPY --from=jre /usr/lib/libz.so.1.3.2 /usr/lib/libz.so.1.3.2

# Identity files for the non-root user and a writable temp directory
# (Tomcat / Spring Boot need java.io.tmpdir).
COPY --from=jre /etc/passwd /etc/passwd
COPY --from=jre /etc/group /etc/group
COPY --from=jre --chmod=1777 /tmp /tmp

# Application
COPY --from=build --chown=10001:10001 /build/application.jar /app/application.jar

USER 10001:10001
WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-XX:MaxRAMPercentage=75.0", "-Djava.io.tmpdir=/tmp", "-jar", "/app/application.jar"]
