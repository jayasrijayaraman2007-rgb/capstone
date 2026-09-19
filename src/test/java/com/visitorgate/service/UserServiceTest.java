package com.visitorgate.service;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

  @Autowired UserService userService;
  @Autowired UserRepository users;
  @Autowired HostRepository hosts;
  @Autowired PasswordEncoder encoder;

  @Test
  void linkedUserCannotBeDeleted() {
    User u = users.save(new User("Linked", "linked-del", encoder.encode("x"), Role.HOST));
    Host h = new Host("Linked", "IT", null, null);
    h.setAccount(u);
    hosts.save(h);
    IllegalStateException e = assertThrows(IllegalStateException.class,
        () -> userService.deleteUser(u.getUserId()));
    assertEquals("User is linked to a host profile and cannot be deleted", e.getMessage());
    assertTrue(users.findByUsername("linked-del").isPresent());
  }

  @Test
  void unlinkedUserCanBeDeleted() {
    User u = users.save(new User("Solo", "solo-del", encoder.encode("x"), Role.SECURITY_OFFICER));
    userService.deleteUser(u.getUserId());
    assertTrue(users.findByUsername("solo-del").isEmpty());
  }
}
