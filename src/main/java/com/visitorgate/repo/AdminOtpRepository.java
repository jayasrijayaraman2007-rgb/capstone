package com.visitorgate.repo;

import com.visitorgate.domain.AdminOtpVerification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOtpRepository extends JpaRepository<AdminOtpVerification, Long> {
  List<AdminOtpVerification> findByUserUserIdOrderByCreatedAtDesc(Long userId);
  Optional<AdminOtpVerification> findFirstByUserUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
  List<AdminOtpVerification> findByExpiresAtBefore(LocalDateTime cutoff);
  List<AdminOtpVerification> findByUsedTrueAndCreatedAtBefore(LocalDateTime cutoff);
}
