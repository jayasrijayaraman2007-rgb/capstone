package com.visitorgate.repo;

import com.visitorgate.domain.Host;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
  List<Host> findByNameContainingIgnoreCase(String name);
}
