package com.visitorgate.service;

import com.visitorgate.domain.AccountStatus;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository users;
  private final HostRepository hosts;
  private final PasswordEncoder encoder;

  public UserService(UserRepository users, HostRepository hosts, PasswordEncoder encoder) {
    this.users = users;
    this.hosts = hosts;
    this.encoder = encoder;
  }

  @Transactional(readOnly = true)
  public List<User> listAll() {
    return users.findAllByOrderByCreatedAtDesc();
  }

  @Transactional(readOnly = true)
  public User findById(Long id) {
    return users.findById(id).orElse(null);
  }

  @Transactional
  public User createAccount(String name, String employeeId, String email, String phone,
      String department, String designation, String username, Role role,
      AccountStatus status) {
    if (role == Role.ADMIN) {
      throw new IllegalArgumentException("Admin accounts cannot be created here");
    }
    requireUnique(username, email, employeeId, null);
    User user = new User(name.trim(), username.trim(), null, role);
    user.setEmployeeId(blankToNull(employeeId));
    user.setEmail(blankToNull(email));
    user.setPhone(blankToNull(phone));
    user.setDepartment(blankToNull(department));
    user.setDesignation(blankToNull(designation));
    user.setStatus(status == null ? AccountStatus.INACTIVE : status);
    return users.save(user);
  }

  @Transactional
  public User setupPassword(String username, String employeeId, String rawPassword,
      String confirmPassword) {
    User user = users.findByUsername(username == null ? "" : username.trim())
        .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    if (user.getPassword() != null) {
      throw new IllegalStateException("Password is already set. Use Change Password instead.");
    }
    if (user.getEmployeeId() == null || employeeId == null
        || !user.getEmployeeId().equals(employeeId.trim())) {
      throw new IllegalArgumentException("Employee ID does not match our records.");
    }
    requirePasswordPair(rawPassword, confirmPassword);
    user.setPassword(encoder.encode(rawPassword));
    user.setStatus(AccountStatus.ACTIVE);
    return user;
  }

  @Transactional
  public void changePassword(String username, String currentPassword, String rawPassword,
      String confirmPassword) {
    User user = users.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    if (user.getPassword() == null || !encoder.matches(
        currentPassword == null ? "" : currentPassword, user.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect.");
    }
    requirePasswordPair(rawPassword, confirmPassword);
    user.setPassword(encoder.encode(rawPassword));
  }

  @Transactional
  public void updateProfile(Long userId, String name, String employeeId, String email,
      String phone, String department, String designation) {
    User user = users.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    requireUnique(user.getUsername(), email, employeeId, userId);
    user.setName(name.trim());
    user.setEmployeeId(blankToNull(employeeId));
    user.setEmail(blankToNull(email));
    user.setPhone(blankToNull(phone));
    user.setDepartment(blankToNull(department));
    user.setDesignation(blankToNull(designation));
  }

  @Transactional
  public void setStatus(Long userId, AccountStatus status, String actingUsername) {
    User user = users.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (user.getUsername().equals(actingUsername)) {
      throw new IllegalStateException("You cannot change your own account status");
    }
    user.setStatus(status);
  }

  @Transactional
  public void deleteUser(Long userId) {
    if (!hosts.findByAccountUserId(userId).isEmpty()) {
      throw new IllegalStateException("User is linked to a host profile and cannot be deleted");
    }
    users.deleteById(userId);
  }

  private void requireUnique(String username, String email, String employeeId, Long selfId) {
    if (selfId == null && users.existsByUsername(username.trim())) {
      throw new IllegalArgumentException("Username already exists.");
    }
    String cleanEmail = blankToNull(email);
    if (cleanEmail != null && users.findAll().stream()
        .anyMatch(u -> !u.getUserId().equals(selfId) && cleanEmail.equalsIgnoreCase(u.getEmail()))) {
      throw new IllegalArgumentException("Email already exists.");
    }
    String cleanEmp = blankToNull(employeeId);
    if (cleanEmp != null && users.findAll().stream()
        .anyMatch(u -> !u.getUserId().equals(selfId) && cleanEmp.equalsIgnoreCase(u.getEmployeeId()))) {
      throw new IllegalArgumentException("Employee ID already exists.");
    }
  }

  private void requirePasswordPair(String rawPassword, String confirmPassword) {
    if (rawPassword == null || !PasswordPolicy.isValid(rawPassword)) {
      throw new IllegalArgumentException(PasswordPolicy.errorMessage(rawPassword));
    }
    if (!rawPassword.equals(confirmPassword)) {
      throw new IllegalArgumentException("Passwords do not match.");
    }
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
