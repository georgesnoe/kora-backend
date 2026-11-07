package com.kora.backend.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enterprises")
public class Enterprise extends BaseEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "currency", nullable = false)
  private String currency;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ownerId", nullable = false)
  private User owner;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "enterprises_users", joinColumns = @JoinColumn(name = "enterpriseId", nullable = false), inverseJoinColumns = @JoinColumn(name = "userId", nullable = false))
  @Builder.Default
  private Set<User> users = new LinkedHashSet<>();

  public void addUser(User user) {
    users.add(user);
  }

  public void removeUser(User user) {
    users.remove(user);
  }

}
