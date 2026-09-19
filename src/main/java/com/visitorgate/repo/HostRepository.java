package com.visitorgate.repo;

import com.visitorgate.domain.Host;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, Long> {
  List<Host> findByNameContainingIgnoreCase(String name);
  Optional<Host> findByEmailIgnoreCase(String email);
  Optional<Host> findByAccountUsername(String username);
}
