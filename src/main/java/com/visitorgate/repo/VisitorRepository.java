package com.visitorgate.repo;

import com.visitorgate.domain.Visitor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
  List<Visitor> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}
