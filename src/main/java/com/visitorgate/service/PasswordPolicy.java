package com.visitorgate.service;

import java.util.regex.Pattern;

public final class PasswordPolicy {

  public static final String REGEX = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{1,8}$";

  private static final Pattern PATTERN = Pattern.compile(REGEX);

  public static final String REQUIREMENTS_TEXT =
      "At least 1 uppercase letter, at least 1 digit, at least 1 special character, maximum 8 characters.";

  private PasswordPolicy() {}

  public static boolean isValid(String password) {
    return password != null && PATTERN.matcher(password).matches();
  }

  public static String errorMessage(String password) {
    if (password == null || password.isEmpty()) {
      return "Password must not be empty.";
    }
    if (password.length() > 8) {
      return "Password must be at most 8 characters.";
    }
    if (!password.chars().anyMatch(Character::isUpperCase)) {
      return "Password must contain at least 1 uppercase letter.";
    }
    if (!password.chars().anyMatch(Character::isDigit)) {
      return "Password must contain at least 1 digit.";
    }
    if (!password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) {
      return "Password must contain at least 1 special character.";
    }
    return "Password does not meet the requirements.";
  }
}
