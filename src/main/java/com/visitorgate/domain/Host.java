package com.visitorgate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "hosts")
public class Host {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "host_id")
  private Long hostId;

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "Department is required")
  @Size(max = 100, message = "Department must be at most 100 characters")
  @Column(nullable = false, length = 100)
  private String department;

  @Size(max = 20, message = "Phone must be at most 20 characters")
  @Column(length = 20)
  private String phone;

  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must be at most 100 characters")
  @Column(length = 100)
  private String email;

  protected Host() {}

  public Host(String name, String department, String phone, String email) {
    this.name = name;
    this.department = department;
    this.phone = phone;
    this.email = email;
  }

  public Long getHostId() { return hostId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDepartment() { return department; }
  public void setDepartment(String department) { this.department = department; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
