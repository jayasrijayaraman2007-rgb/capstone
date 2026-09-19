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
@Table(name = "visitors")
public class Visitor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "visitor_id")
  private Long visitorId;

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  @Column(nullable = false, length = 100)
  private String name;

  @NotBlank(message = "Phone is required")
  @Size(max = 20, message = "Phone must be at most 20 characters")
  @Column(nullable = false, length = 20)
  private String phone;

  @Email(message = "Email must be valid")
  @Size(max = 100, message = "Email must be at most 100 characters")
  @Column(length = 100)
  private String email;

  @Size(max = 255, message = "Address must be at most 255 characters")
  @Column(length = 255)
  private String address;

  @NotBlank(message = "ID proof is required")
  @Size(max = 100, message = "ID proof must be at most 100 characters")
  @Column(name = "id_proof", nullable = false, length = 100)
  private String idProof;

  protected Visitor() {}

  public Visitor(String name, String phone, String email, String address, String idProof) {
    this.name = name;
    this.phone = phone;
    this.email = email;
    this.address = address;
    this.idProof = idProof;
  }

  public Long getVisitorId() { return visitorId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getIdProof() { return idProof; }
  public void setIdProof(String idProof) { this.idProof = idProof; }
}
