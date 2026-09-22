package com.visitorgate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AdminMailService {

  private static final Logger log = LoggerFactory.getLogger(AdminMailService.class);

  private final ObjectProvider<JavaMailSender> mailSender;
  private final String host;
  private final String from;

  public AdminMailService(ObjectProvider<JavaMailSender> mailSender,
      @Value("${spring.mail.host:}") String host,
      @Value("${spring.mail.from:}") String from) {
    this.mailSender = mailSender;
    this.host = host == null ? "" : host.trim();
    this.from = from == null ? "" : from.trim();
  }

  public boolean isConfigured() {
    return !host.isEmpty() && mailSender.getIfAvailable() != null;
  }

  public void sendVerificationCode(String toEmail, String code) {
    JavaMailSender sender = mailSender.getIfAvailable();
    if (sender == null || !isConfigured()) {
      throw new IllegalStateException("Email service is not configured.");
    }
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    if (!from.isEmpty()) {
      message.setFrom(from);
    }
    message.setSubject("Sovereign Gate — Admin Login Verification Code");
    message.setText("SOVEREIGN GATE\n\nAdmin Login Verification\n\n"
        + "Your verification code is:\n\n" + code + "\n\n"
        + "This code expires in 5 minutes.\n\n"
        + "If you did not request this verification code, you can safely ignore this email.");
    sender.send(message);
    log.info("Admin verification code sent to requested address.");
  }
}
