package com.visitorgate.service;

import com.visitorgate.domain.AccountStatus;
import com.visitorgate.domain.AdminOtpVerification;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.AdminOtpRepository;
import com.visitorgate.repo.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOtpService {

  public static final int OTP_EXPIRY_MINUTES = 5;
  public static final int MAX_ATTEMPTS = 5;
  public static final int RESEND_COOLDOWN_SECONDS = 60;
  public static final int MAX_REQUESTS_PER_HOUR = 5;

  public enum RequestResult { SENT, NOT_CONFIGURED, COOLDOWN, RATE_LIMITED, UNKNOWN }

  public enum VerifyResult { OK, INVALID, EXPIRED, LOCKED, NOT_FOUND }

  private final AdminOtpRepository otps;
  private final UserRepository users;
  private final AdminMailService mail;
  private final OtpCodeGenerator generator;
  private final Clock clock;

  public AdminOtpService(AdminOtpRepository otps, UserRepository users, AdminMailService mail,
      OtpCodeGenerator generator, Clock clock) {
    this.otps = otps;
    this.users = users;
    this.mail = mail;
    this.generator = generator;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public Optional<User> findEligibleAdmin(String rawEmail) {
    String email = normalize(rawEmail);
    if (email.isEmpty()) {
      return Optional.empty();
    }
    return users.findAll().stream()
        .filter(u -> u.getRole() == Role.ADMIN
            && u.getStatus() == AccountStatus.ACTIVE
            && email.equalsIgnoreCase(u.getEmail()))
        .findFirst();
  }

  @Transactional
  public RequestResult requestCode(String rawEmail) {
    Optional<User> admin = findEligibleAdmin(rawEmail);
    if (admin.isEmpty()) {
      return RequestResult.UNKNOWN;
    }
    User user = admin.get();
    LocalDateTime now = LocalDateTime.now(clock);
    List<AdminOtpVerification> history = otps.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    if (!history.isEmpty()
        && history.get(0).getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(now)) {
      return RequestResult.COOLDOWN;
    }
    long recent = history.stream()
        .filter(r -> r.getCreatedAt().plusHours(1).isAfter(now))
        .count();
    if (recent >= MAX_REQUESTS_PER_HOUR) {
      return RequestResult.RATE_LIMITED;
    }
    if (!mail.isConfigured()) {
      return RequestResult.NOT_CONFIGURED;
    }
    history.stream().filter(r -> !r.isUsed()).forEach(AdminOtpVerification::markUsed);
    String code = generator.generate();
    AdminOtpVerification record = new AdminOtpVerification(user, user.getEmail(),
        sha256(code), now.plusMinutes(OTP_EXPIRY_MINUTES));
    otps.save(record);
    mail.sendVerificationCode(user.getEmail(), code);
    return RequestResult.SENT;
  }

  @Transactional
  public VerifyResult verifyCode(String rawEmail, String rawCode) {
    Optional<User> admin = findEligibleAdmin(rawEmail);
    if (admin.isEmpty()) {
      return VerifyResult.NOT_FOUND;
    }
    Optional<AdminOtpVerification> current =
        otps.findFirstByUserUserIdAndUsedFalseOrderByCreatedAtDesc(admin.get().getUserId());
    if (current.isEmpty()) {
      return VerifyResult.NOT_FOUND;
    }
    AdminOtpVerification record = current.get();
    LocalDateTime now = LocalDateTime.now(clock);
    if (!record.getExpiresAt().isAfter(now)) {
      record.markUsed();
      return VerifyResult.EXPIRED;
    }
    if (record.getAttemptCount() >= MAX_ATTEMPTS) {
      record.markUsed();
      return VerifyResult.LOCKED;
    }
    String code = rawCode == null ? "" : rawCode.trim();
    if (!sha256(code).equals(record.getOtpHash())) {
      record.incrementAttempts();
      if (record.getAttemptCount() >= MAX_ATTEMPTS) {
        record.markUsed();
        return VerifyResult.LOCKED;
      }
      return VerifyResult.INVALID;
    }
    record.markUsed();
    return VerifyResult.OK;
  }

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void purgeOldRecords() {
    LocalDateTime now = LocalDateTime.now(clock);
    otps.findByExpiresAtBefore(now).forEach(otps::delete);
    otps.findByUsedTrueAndCreatedAtBefore(now.minusHours(24)).forEach(otps::delete);
  }

  public static String normalize(String email) {
    if (email == null) {
      return "";
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
