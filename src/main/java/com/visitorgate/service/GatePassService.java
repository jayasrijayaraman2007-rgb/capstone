package com.visitorgate.service;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.repo.GatePassRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatePassService {

  private final GatePassRepository passes;
  private final VisitRequestService requests;

  public GatePassService(GatePassRepository passes, VisitRequestService requests) {
    this.passes = passes;
    this.requests = requests;
  }

  @Transactional
  public GatePass generateFromRequest(Long requestId) {
    VisitRequest request = requests.findById(requestId);
    if (request == null) {
      throw new IllegalArgumentException("Request not found");
    }
    if (request.getStatus() != RequestStatus.APPROVED) {
      throw new IllegalStateException("Only approved requests get a gate pass");
    }
    return passes.findByRequestRequestId(requestId)
        .orElseGet(() -> passes.save(new GatePass(request)));
  }

  @Transactional(readOnly = true)
  public GatePass findById(Long id) {
    return passes.findById(id).orElse(null);
  }

  @Transactional(readOnly = true)
  public GatePass findByRequestId(Long requestId) {
    return passes.findByRequestRequestId(requestId).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<GatePass> list(PassStatus status) {
    if (status == null) {
      return passes.findAll();
    }
    return passes.findByStatus(status);
  }

  @Transactional(readOnly = true)
  public long totalCount() {
    return passes.count();
  }

  @Transactional(readOnly = true)
  public java.util.Map<String, Long> statusCounts() {
    java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
    for (PassStatus s : PassStatus.values()) {
      counts.put(s.name(), passes.countByStatus(s));
    }
    return counts;
  }
}
