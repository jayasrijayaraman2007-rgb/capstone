package com.visitorgate.repo;

import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRequestRepository extends JpaRepository<VisitRequest, Long> {
  List<VisitRequest> findByStatus(RequestStatus status);
  List<VisitRequest> findByHostHostId(Long hostId);
  List<VisitRequest> findByHostHostIdAndStatus(Long hostId, RequestStatus status);
}
