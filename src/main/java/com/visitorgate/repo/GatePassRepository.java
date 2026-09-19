package com.visitorgate.repo;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatePassRepository extends JpaRepository<GatePass, Long> {
  Optional<GatePass> findByRequestRequestId(Long requestId);
  List<GatePass> findByStatus(PassStatus status);
}
