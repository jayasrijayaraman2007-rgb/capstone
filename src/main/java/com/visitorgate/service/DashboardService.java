package com.visitorgate.service;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.repo.GatePassRepository;
import com.visitorgate.repo.VisitRequestRepository;
import com.visitorgate.repo.VisitorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private final VisitorRepository visitors;
  private final VisitRequestRepository requests;
  private final GatePassRepository passes;

  public DashboardService(VisitorRepository visitors, VisitRequestRepository requests,
      GatePassRepository passes) {
    this.visitors = visitors;
    this.requests = requests;
    this.passes = passes;
  }

  @Transactional(readOnly = true)
  public DashboardStats stats() {
    return new DashboardStats(
        visitors.count(),
        requests.countByStatus(RequestStatus.PENDING),
        requests.countByStatus(RequestStatus.APPROVED),
        passes.countByStatus(PassStatus.ACTIVE),
        passes.countByStatus(PassStatus.COMPLETED),
        passes.findTop5ByOrderByIssueDateDesc(),
        requests.findTop5ByOrderByRequestDateDesc());
  }

  public record DashboardStats(
      long totalVisitors,
      long pendingRequests,
      long approvedRequests,
      long insideNow,
      long completedVisits,
      List<GatePass> recentPasses,
      List<VisitRequest> recentRequests) {}
}
