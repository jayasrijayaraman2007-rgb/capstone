package com.visitorgate.service;

import com.visitorgate.domain.Host;
import com.visitorgate.repo.HostRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HostService {

  private final HostRepository hosts;

  public HostService(HostRepository hosts) {
    this.hosts = hosts;
  }

  public List<Host> search(String q) {
    if (q == null || q.isBlank()) {
      return hosts.findAll();
    }
    return hosts.findByNameContainingIgnoreCase(q.trim());
  }

  public List<Host> findAll() {
    return hosts.findAll();
  }

  public Host findById(Long id) {
    return hosts.findById(id).orElse(null);
  }

  @Transactional
  public Host save(Host host) {
    return hosts.save(host);
  }
}
