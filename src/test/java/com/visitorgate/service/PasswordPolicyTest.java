package com.visitorgate.service;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyTest {

  static Stream<String> validPasswords() {
    return Stream.of("A1@hello", "P2#test", "X9!abcde", "A1@a", "Z9$");
  }

  @ParameterizedTest
  @MethodSource("validPasswords")
  void validPasswordsPass(String password) {
    assertTrue(PasswordPolicy.isValid(password));
  }

  @ParameterizedTest
  @ValueSource(strings = {"password", "Password", "Password1", "PASS@ABC", "pass@123",
      "A12345678@", "", "A1@hello99", "abcdefg!", "ABCDEFG1", "Abcdef12"})
  void invalidPasswordsFail(String password) {
    assertFalse(PasswordPolicy.isValid(password));
  }

  @Test
  void messagesIdentifyEachMissingRule() {
    assertEquals("Password must not be empty.", PasswordPolicy.errorMessage(""));
    assertEquals("Password must be at most 8 characters.", PasswordPolicy.errorMessage("A12345678@"));
    assertEquals("Password must contain at least 1 uppercase letter.",
        PasswordPolicy.errorMessage("pass@123"));
    assertEquals("Password must contain at least 1 digit.",
        PasswordPolicy.errorMessage("Passwor@"));
    assertEquals("Password must contain at least 1 special character.",
        PasswordPolicy.errorMessage("Passwor1"));
  }
}
