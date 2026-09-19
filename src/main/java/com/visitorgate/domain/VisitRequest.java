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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_requests")
public class VisitRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "request_id")
  private Long requestId;

  @NotNull(message = "Visitor is required")
  @ManyToOne(optional = false)
  @JoinColumn(name = "visitor_id", nullable = false)
  private Visitor visitor;

  @NotNull(message = "Host is required")
  @ManyToOne(optional = false)
  @JoinColumn(name = "host_id", nullable = false)
  private Host host;

  @NotBlank(message = "Purpose is required")
  @Size(max = 255, message = "Purpose must be at most 255 characters")
  @Column(nullable = false, length = 255)
  private String purpose;

  @Column(name = "request_date", nullable = false)
  private LocalDateTime requestDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RequestStatus status;

  protected VisitRequest() {}

  public VisitRequest(Visitor visitor, Host host, String purpose) {
    this.visitor = visitor;
    this.host = host;
    this.purpose = purpose;
    this.requestDate = LocalDateTime.now();
    this.status = RequestStatus.PENDING;
  }

  public Long getRequestId() { return requestId; }
  public Visitor getVisitor() { return visitor; }
  public void setVisitor(Visitor visitor) { this.visitor = visitor; }
  public Host getHost() { return host; }
  public void setHost(Host host) { this.host = host; }
  public String getPurpose() { return purpose; }
  public void setPurpose(String purpose) { this.purpose = purpose; }
  public LocalDateTime getRequestDate() { return requestDate; }
  public RequestStatus getStatus() { return status; }

  public void approve() {
    requirePending();
    this.status = RequestStatus.APPROVED;
  }

  public void reject() {
    requirePending();
    this.status = RequestStatus.REJECTED;
  }

  private void requirePending() {
    if (this.status != RequestStatus.PENDING) {
      throw new IllegalStateException("Only pending requests can be decided");
    }
  }
}
