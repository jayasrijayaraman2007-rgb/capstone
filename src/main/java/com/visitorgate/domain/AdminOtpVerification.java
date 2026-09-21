package com.visitorgate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_otp_verifications")
public class AdminOtpVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String email;

  @Column(name = "otp_hash", nullable = false, length = 64)
  private String otpHash;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount = 0;

  @Column(nullable = false)
  private boolean used = false;

  protected AdminOtpVerification() {}

  public AdminOtpVerification(User user, String email, String otpHash, LocalDateTime expiresAt) {
    this.user = user;
    this.email = email;
    this.otpHash = otpHash;
    this.expiresAt = expiresAt;
  }

  public Long getId() { return id; }
  public User getUser() { return user; }
  public String getEmail() { return email; }
  public String getOtpHash() { return otpHash; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getExpiresAt() { return expiresAt; }
  public int getAttemptCount() { return attemptCount; }
  public void incrementAttempts() { this.attemptCount++; }
  public boolean isUsed() { return used; }
  public void markUsed() { this.used = true; }
  /** Allows backdating in tests and data repair; not used by the web flow. */
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
