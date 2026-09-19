package com.visitorgate.repo;

import com.visitorgate.domain.EntryExit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryExitRepository extends JpaRepository<EntryExit, Long> {
  Optional<EntryExit> findByPassPassId(Long passId);
  List<EntryExit> findAllByOrderByEntryTimeDesc();
}
