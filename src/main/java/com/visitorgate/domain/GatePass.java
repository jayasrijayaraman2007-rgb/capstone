package com.visitorgate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_passes")
public class GatePass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pass_id")
  private Long passId;

  @NotNull
  @OneToOne(optional = false)
  @JoinColumn(name = "request_id", nullable = false, unique = true)
  private VisitRequest request;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "visitor_id", nullable = false)
  private Visitor visitor;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "host_id", nullable = false)
  private Host host;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String purpose;

  @Column(name = "issue_date", nullable = false)
  private LocalDateTime issueDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PassStatus status;

  protected GatePass() {}

  public GatePass(VisitRequest request) {
    this.request = request;
    this.visitor = request.getVisitor();
    this.host = request.getHost();
    this.purpose = request.getPurpose();
    this.issueDate = LocalDateTime.now();
    this.status = PassStatus.APPROVED;
  }

  public Long getPassId() { return passId; }
  public VisitRequest getRequest() { return request; }
  public Visitor getVisitor() { return visitor; }
  public Host getHost() { return host; }
  public String getPurpose() { return purpose; }
  public LocalDateTime getIssueDate() { return issueDate; }
  public PassStatus getStatus() { return status; }
  public void setStatus(PassStatus status) { this.status = status; }
}
