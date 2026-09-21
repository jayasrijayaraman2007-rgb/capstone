package com.visitorgate.service;

import com.visitorgate.domain.AccountStatus;
import com.visitorgate.domain.AdminOtpVerification;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.AdminOtpRepository;
import com.visitorgate.repo.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminOtpServiceTest {

  @TestConfiguration
  static class FixedTimeConfig {
    @Bean
    @Primary
    FixedGenerator generator() {
      return new FixedGenerator();
    }
  }

  static class FixedGenerator implements OtpCodeGenerator {
    volatile String code = "482731";

    @Override
    public String generate() {
      return code;
    }
  }

  @Autowired FixedGenerator fixedGenerator;

  @Autowired AdminOtpService otpService;
  @Autowired AdminOtpRepository otps;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;
  @MockitoBean AdminMailService mail;

  private User admin;
  private User hostUser;
  private User inactiveAdmin;

  @BeforeEach
  void setUp() {
    when(mail.isConfigured()).thenReturn(true);
    fixedGenerator.code = "482731";
    admin = users.save(new User("Otp Admin", "otp-admin", encoder.encode("x"), Role.ADMIN));
    admin.setEmail("otp-admin@example.com");
    hostUser = users.save(new User("Otp Host", "otp-host", encoder.encode("x"), Role.HOST));
    hostUser.setEmail("otp-host@example.com");
    inactiveAdmin = users.save(new User("Off Admin", "off-admin", encoder.encode("x"), Role.ADMIN));
    inactiveAdmin.setEmail("off-admin@example.com");
    inactiveAdmin.setStatus(AccountStatus.INACTIVE);
  }

  private void backdateLatest(User user, long minutesAgo) {
    List<AdminOtpVerification> all =
        otps.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    all.get(0).setCreatedAt(LocalDateTime.now().minusMinutes(minutesAgo));
  }

  @Test
  void validAdminEmailRequestsOtp() {
    assertEquals(AdminOtpService.RequestResult.SENT, otpService.requestCode("otp-admin@example.com"));
    verify(mail).sendVerificationCode(eq("otp-admin@example.com"), eq("482731"));
    List<AdminOtpVerification> records =
        otps.findByUserUserIdOrderByCreatedAtDesc(admin.getUserId());
    assertEquals(1, records.size());
    assertNotEquals("482731", records.get(0).getOtpHash());
    assertEquals(64, records.get(0).getOtpHash().length());
  }

  @Test
  void unknownNonAdminAndInactiveGetUnknown() {
    assertEquals(AdminOtpService.RequestResult.UNKNOWN, otpService.requestCode("nobody@example.com"));
    assertEquals(AdminOtpService.RequestResult.UNKNOWN, otpService.requestCode("otp-host@example.com"));
    assertEquals(AdminOtpService.RequestResult.UNKNOWN, otpService.requestCode("off-admin@example.com"));
    verify(mail, never()).sendVerificationCode(anyString(), anyString());
    assertTrue(otps.findAll().isEmpty());
  }

  @Test
  void correctOtpVerifies() {
    otpService.requestCode("otp-admin@example.com");
    assertEquals(AdminOtpService.VerifyResult.OK,
        otpService.verifyCode("otp-admin@example.com", "482731"));
  }

  @Test
  void incorrectOtpRejectedAndCountsAttempts() {
    otpService.requestCode("otp-admin@example.com");
    assertEquals(AdminOtpService.VerifyResult.INVALID,
        otpService.verifyCode("otp-admin@example.com", "000000"));
    assertEquals(AdminOtpService.VerifyResult.INVALID,
        otpService.verifyCode("otp-admin@example.com", "111111"));
    AdminOtpVerification record =
        otps.findByUserUserIdOrderByCreatedAtDesc(admin.getUserId()).get(0);
    assertEquals(2, record.getAttemptCount());
    assertEquals(AdminOtpService.VerifyResult.OK,
        otpService.verifyCode("otp-admin@example.com", "482731"));
  }

  @Test
  void expiredOtpRejected() {
    User u = admin;
    AdminOtpVerification expired = new AdminOtpVerification(u, u.getEmail(),
        AdminOtpService.sha256("482731"), LocalDateTime.now().minusMinutes(10));
    otps.save(expired);
    assertEquals(AdminOtpService.VerifyResult.EXPIRED,
        otpService.verifyCode("otp-admin@example.com", "482731"));
  }

  @Test
  void usedOtpCannotBeReused() {
    otpService.requestCode("otp-admin@example.com");
    assertEquals(AdminOtpService.VerifyResult.OK,
        otpService.verifyCode("otp-admin@example.com", "482731"));
    assertEquals(AdminOtpService.VerifyResult.NOT_FOUND,
        otpService.verifyCode("otp-admin@example.com", "482731"));
  }

  @Test
  void newOtpInvalidatesPrevious() {
    otpService.requestCode("otp-admin@example.com");
    backdateLatest(admin, 2);
    fixedGenerator.code = "999999";
    assertEquals(AdminOtpService.RequestResult.SENT, otpService.requestCode("otp-admin@example.com"));
    assertEquals(AdminOtpService.VerifyResult.INVALID,
        otpService.verifyCode("otp-admin@example.com", "482731"));
    assertEquals(AdminOtpService.VerifyResult.OK,
        otpService.verifyCode("otp-admin@example.com", "999999"));
  }

  @Test
  void attemptLimitLocks() {
    otpService.requestCode("otp-admin@example.com");
    for (int i = 0; i < 4; i++) {
      assertEquals(AdminOtpService.VerifyResult.INVALID,
          otpService.verifyCode("otp-admin@example.com", "00000" + i));
    }
    assertEquals(AdminOtpService.VerifyResult.LOCKED,
        otpService.verifyCode("otp-admin@example.com", "000004"));
    assertEquals(AdminOtpService.VerifyResult.NOT_FOUND,
        otpService.verifyCode("otp-admin@example.com", "482731"));
  }

  @Test
  void resendCooldownAndHourlyCap() {
    assertEquals(AdminOtpService.RequestResult.SENT, otpService.requestCode("otp-admin@example.com"));
    assertEquals(AdminOtpService.RequestResult.COOLDOWN, otpService.requestCode("otp-admin@example.com"));
    for (int i = 0; i < 4; i++) {
      backdateLatest(admin, 2 + i);
      AdminOtpVerification extra = new AdminOtpVerification(admin, admin.getEmail(),
          AdminOtpService.sha256("x" + i), LocalDateTime.now().plusMinutes(4));
      extra.setCreatedAt(LocalDateTime.now().minusMinutes(50 - i * 5));
      otps.save(extra);
    }
    backdateLatest(admin, 2);
    assertEquals(AdminOtpService.RequestResult.RATE_LIMITED,
        otpService.requestCode("otp-admin@example.com"));
  }
}
