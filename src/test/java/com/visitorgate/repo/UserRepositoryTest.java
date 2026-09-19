package com.visitorgate.repo;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
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
class UserRepositoryTest {

  @Autowired UserRepository users;

  @Test
  void saveAndFindByUsername() {
    users.save(new User("Admin", "admin1", "hashed", Role.ADMIN));
    assertTrue(users.existsByUsername("admin1"));
    assertEquals(Role.ADMIN, users.findByUsername("admin1").orElseThrow().getRole());
  }
}
