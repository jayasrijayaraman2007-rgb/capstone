package com.visitorgate.service;

import com.visitorgate.domain.Visitor;
import com.visitorgate.repo.VisitorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitorService {

  private final VisitorRepository visitors;

  public VisitorService(VisitorRepository visitors) {
    this.visitors = visitors;
  }

  public List<Visitor> search(String q) {
    if (q == null || q.isBlank()) {
      return visitors.findAll();
    }
    return visitors.findByNameContainingIgnoreCaseOrPhoneContaining(q.trim(), q.trim());
  }

  public Visitor findById(Long id) {
    return visitors.findById(id).orElse(null);
  }

  @Transactional
  public Visitor save(Visitor visitor) {
    return visitors.save(visitor);
  }
}
