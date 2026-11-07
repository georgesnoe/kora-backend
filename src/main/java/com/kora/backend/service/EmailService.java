package com.kora.backend.service;

import com.kora.backend.config.MailProperties;
import com.kora.backend.config.VerificationProperties;
import com.kora.backend.entity.User;
import com.kora.backend.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Locale;

@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailProperties mailProperties;
	private final VerificationProperties verificationProperties;

	public EmailService(
			JavaMailSender mailSender,
			SpringTemplateEngine templateEngine,
			MailProperties mailProperties,
			VerificationProperties verificationProperties) {

		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.mailProperties = mailProperties;
		this.verificationProperties = verificationProperties;
	}

	public void sendVerificationEmail(User user, String code) {
		Context context = new Context(Locale.getDefault());
		context.setVariable("name", user.getName());
		context.setVariable("email", user.getEmail());
		context.setVariable("code", code);
		context.setVariable("verifyUrl", buildVerifyUrl(user.getEmail(), code));
		context.setVariable("expiresInHours", Math.max(1, verificationProperties.ttl().toHours()));
		context.setVariable("year", Year.now().getValue());

		String html = templateEngine.process("email/verify-account", context);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(
					message, false, StandardCharsets.UTF_8.name());

			helper.setFrom(mailProperties.from(), mailProperties.fromName());
			helper.setTo(user.getEmail());
			helper.setSubject("Verify your Kora account");
			helper.setText(html, true);

			mailSender.send(message);
		}
		catch (MessagingException | UnsupportedEncodingException | MailException exception) {
			throw new EmailSendingException(
					"Could not send verification email to " + user.getEmail(), exception);
		}
	}

	private String buildVerifyUrl(String email, String code) {
		String baseUrl = verificationProperties.verifyUrl();
		String separator = baseUrl.contains("?") ? "&" : "?";

		return baseUrl + separator
				+ "identifier=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
				+ "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
	}

}
