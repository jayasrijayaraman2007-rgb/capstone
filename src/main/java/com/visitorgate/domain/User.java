package com.visitorgate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank
  @Size(max = 50)
  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @NotBlank
  @Column(nullable = false, length = 100)
  private String password;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Size(max = 50, message = "Employee ID must be at most 50 characters")
  @Column(name = "employee_id", unique = true, length = 50)
  private String employeeId;

  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must be at most 100 characters")
  @Column(unique = true, length = 100)
  private String email;

  @Size(max = 20, message = "Phone must be at most 20 characters")
  @Column(length = 20)
  private String phone;

  @Size(max = 100, message = "Department must be at most 100 characters")
  @Column(length = 100)
  private String department;

  @Size(max = 100, message = "Designation must be at most 100 characters")
  @Column(length = 100)
  private String designation;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected User() {}

  public User(String name, String username, String password, Role role) {
    this.name = name;
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public Long getUserId() { return userId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public Role getRole() { return role; }
  public void setRole(Role role) { this.role = role; }
  public String getEmployeeId() { return employeeId; }
  public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getDepartment() { return department; }
  public void setDepartment(String department) { this.department = department; }
  public String getDesignation() { return designation; }
  public void setDesignation(String designation) { this.designation = designation; }
  public AccountStatus getStatus() { return status; }
  public void setStatus(AccountStatus status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
