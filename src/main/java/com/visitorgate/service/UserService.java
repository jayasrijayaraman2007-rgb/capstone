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

  public static final int MIN_PASSWORD_LENGTH = 8;

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
      String department, String designation, String username, String rawPassword,
      Role role, AccountStatus status) {
    requireUnique(username, email, employeeId, null);
    requirePassword(rawPassword);
    if (role == Role.ADMIN) {
      throw new IllegalArgumentException("Admin accounts cannot be created here");
    }
    User user = new User(name.trim(), username.trim(), encoder.encode(rawPassword), role);
    user.setEmployeeId(blankToNull(employeeId));
    user.setEmail(blankToNull(email));
    user.setPhone(blankToNull(phone));
    user.setDepartment(blankToNull(department));
    user.setDesignation(blankToNull(designation));
    user.setStatus(status == null ? AccountStatus.ACTIVE : status);
    return users.save(user);
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

  private void requirePassword(String rawPassword) {
    if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException(
          "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
    }
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
