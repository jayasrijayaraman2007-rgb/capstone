package com.visitorgate.service;

import com.visitorgate.domain.EntryExit;
import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import com.visitorgate.repo.EntryExitRepository;
import com.visitorgate.repo.GatePassRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntryExitService {

  private final EntryExitRepository records;
  private final GatePassRepository passes;

  public EntryExitService(EntryExitRepository records, GatePassRepository passes) {
    this.records = records;
    this.passes = passes;
  }

  @Transactional
  public EntryExit recordEntry(Long passId) {
    GatePass pass = passes.findById(passId)
        .orElseThrow(() -> new IllegalArgumentException("Gate pass not found"));
    if (pass.getStatus() != PassStatus.APPROVED || records.findByPassPassId(passId).isPresent()) {
      throw new IllegalStateException("Entry can only be recorded once for an approved pass");
    }
    pass.setStatus(PassStatus.ACTIVE);
    return records.save(new EntryExit(pass, LocalDateTime.now()));
  }

  @Transactional
  public EntryExit recordExit(Long passId) {
    GatePass pass = passes.findById(passId)
        .orElseThrow(() -> new IllegalArgumentException("Gate pass not found"));
    EntryExit record = records.findByPassPassId(passId).orElse(null);
    if (pass.getStatus() != PassStatus.ACTIVE || record == null || record.getExitTime() != null) {
      throw new IllegalStateException("Exit can only be recorded once after entry");
    }
    record.setExitTime(LocalDateTime.now());
    pass.setStatus(PassStatus.COMPLETED);
    return record;
  }

  @Transactional(readOnly = true)
  public EntryExit findByPassId(Long passId) {
    return records.findByPassPassId(passId).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<EntryExit> history() {
    return records.findAllByOrderByEntryTimeDesc();
  }
}
