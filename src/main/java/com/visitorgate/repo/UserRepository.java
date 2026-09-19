package com.visitorgate.repo;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  boolean existsByUsername(String username);
  List<User> findByRole(Role role);
}
