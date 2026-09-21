package com.visitorgate.config;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class DataSeeder {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  @Bean
  ApplicationRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
      @Value("${admin.username:admin}") String adminUsername,
      @Value("${admin.password:}") String adminPassword,
      @Value("${admin.email:}") String adminEmail) {
    return args -> {
      users.findByUsername(adminUsername).ifPresent(existing -> {
        if (existing.getEmail() == null && adminEmail != null && !adminEmail.isBlank()) {
          existing.setEmail(adminEmail.trim());
          log.info("Linked admin email for '{}'.", adminUsername);
        }
      });
      if (users.existsByUsername(adminUsername)) {
        return;
      }
      if (adminPassword == null || adminPassword.isBlank()) {
        log.warn("ADMIN_PASSWORD not set — skipping default admin seed. Set ADMIN_PASSWORD env var to create '{}'.", adminUsername);
        return;
      }
      User admin = new User("Administrator", adminUsername, encoder.encode(adminPassword), Role.ADMIN);
      if (adminEmail != null && !adminEmail.isBlank()) {
        admin.setEmail(adminEmail.trim());
      }
      users.save(admin);
      log.info("Seeded default admin user '{}'.", adminUsername);
    };
  }
}
