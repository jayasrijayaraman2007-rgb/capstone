package com.visitorgate.repo;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.domain.Visitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class VisitRequestRepositoryTest {

  @Autowired VisitRequestRepository requests;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;

  @Test
  void saveAndQueryByStatusAndHost() {
    Visitor v = visitors.save(new Visitor("Req Visitor", "9000000001", null, null, "ID-1"));
    Host h = hosts.save(new Host("Req Host", "IT", null, "reqhost@example.com"));
    VisitRequest r = requests.save(new VisitRequest(v, h, "Interview"));
    assertEquals(RequestStatus.PENDING, r.getStatus());
    assertTrue(requests.findByStatus(RequestStatus.PENDING).stream()
        .anyMatch(vr -> vr.getRequestId().equals(r.getRequestId())));
    assertEquals(1, requests.findByHostHostId(h.getHostId()).size());
    assertTrue(requests.findByHostHostIdAndStatus(h.getHostId(), RequestStatus.APPROVED).isEmpty());
  }
}
