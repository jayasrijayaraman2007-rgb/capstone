package com.visitorgate.repo;

import com.visitorgate.domain.Host;
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
class HostRepositoryTest {

  @Autowired HostRepository hosts;

  @Test
  void saveAndSearchByName() {
    hosts.save(new Host("Priya Sharma", "Admissions", "9811100000", "priya@example.com"));
    assertEquals(1, hosts.findByNameContainingIgnoreCase("priya").size());
    assertTrue(hosts.findByNameContainingIgnoreCase("nobody").isEmpty());
  }
}
