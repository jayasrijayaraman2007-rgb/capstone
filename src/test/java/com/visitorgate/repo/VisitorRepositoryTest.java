package com.visitorgate.repo;

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
class VisitorRepositoryTest {

  @Autowired VisitorRepository visitors;

  @Test
  void saveAndSearchByNameOrPhone() {
    visitors.save(new Visitor("Meera Nair", "9876501234", "meera@example.com", "Chennai", "Aadhaar XXXX"));
    assertEquals(1, visitors.findByNameContainingIgnoreCaseOrPhoneContaining("meera", "meera").size());
    assertEquals(1, visitors.findByNameContainingIgnoreCaseOrPhoneContaining("98765", "98765").size());
    assertTrue(visitors.findByNameContainingIgnoreCaseOrPhoneContaining("nobody", "nobody").isEmpty());
  }
}
