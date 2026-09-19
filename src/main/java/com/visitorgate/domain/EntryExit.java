package com.visitorgate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "entry_exit")
public class EntryExit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "entry_exit_id")
  private Long entryExitId;

  @OneToOne(optional = false)
  @JoinColumn(name = "pass_id", nullable = false, unique = true)
  private GatePass pass;

  @Column(name = "entry_time")
  private LocalDateTime entryTime;

  @Column(name = "exit_time")
  private LocalDateTime exitTime;

  protected EntryExit() {}

  public EntryExit(GatePass pass, LocalDateTime entryTime) {
    this.pass = pass;
    this.entryTime = entryTime;
  }

  public Long getEntryExitId() { return entryExitId; }
  public GatePass getPass() { return pass; }
  public LocalDateTime getEntryTime() { return entryTime; }
  public LocalDateTime getExitTime() { return exitTime; }
  public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
}
