package com.visitorgate.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureOtpCodeGenerator implements OtpCodeGenerator {

  private final SecureRandom random = new SecureRandom();

  @Override
  public String generate() {
    return String.format("%06d", random.nextInt(1_000_000));
  }
}
