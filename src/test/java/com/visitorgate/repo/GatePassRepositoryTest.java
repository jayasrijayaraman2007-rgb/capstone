package com.visitorgate.repo;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.Host;
import com.visitorgate.domain.PassStatus;
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
class GatePassRepositoryTest {

  @Autowired GatePassRepository passes;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;
  @Autowired VisitRequestRepository requests;

  @Test
  void saveAndQuery() {
    Visitor v = visitors.save(new Visitor("Pass Visitor", "9000000003", null, null, "ID-3"));
    Host h = hosts.save(new Host("Pass Host", "Ops", null, "passhost@example.com"));
    VisitRequest r = requests.save(new VisitRequest(v, h, "Maintenance"));
    r.approve();
    GatePass p = passes.save(new GatePass(r));
    assertEquals(PassStatus.APPROVED, p.getStatus());
    assertTrue(passes.findByRequestRequestId(r.getRequestId()).isPresent());
    assertEquals(1, passes.findByStatus(PassStatus.APPROVED).size());
  }
}
