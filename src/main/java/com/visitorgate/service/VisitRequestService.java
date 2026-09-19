package com.visitorgate.service;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.VisitRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitRequestService {

  private final VisitRequestRepository requests;
  private final HostRepository hosts;

  public VisitRequestService(VisitRequestRepository requests, HostRepository hosts) {
    this.requests = requests;
    this.hosts = hosts;
  }

  @Transactional(readOnly = true)
  public List<VisitRequest> listForAdmin(RequestStatus status) {
    if (status == null) {
      return requests.findAll();
    }
    return requests.findByStatus(status);
  }

  @Transactional(readOnly = true)
  public List<VisitRequest> listForHostAccount(String username, RequestStatus status) {
    Host host = username == null ? null : hosts.findByEmailIgnoreCase(username).orElse(null);
    if (host == null) {
      return List.of();
    }
    if (status == null) {
      return requests.findByHostHostId(host.getHostId());
    }
    return requests.findByHostHostIdAndStatus(host.getHostId(), status);
  }

  @Transactional(readOnly = true)
  public VisitRequest findById(Long id) {
    return requests.findById(id).orElse(null);
  }

  @Transactional
  public VisitRequest save(VisitRequest request) {
    return requests.save(request);
  }

  @Transactional
  public void approve(Long id) {
    VisitRequest request = findById(id);
    if (request == null) {
      throw new IllegalArgumentException("Request not found");
    }
    request.approve();
  }

  @Transactional
  public void reject(Long id) {
    VisitRequest request = findById(id);
    if (request == null) {
      throw new IllegalArgumentException("Request not found");
    }
    request.reject();
  }
}
