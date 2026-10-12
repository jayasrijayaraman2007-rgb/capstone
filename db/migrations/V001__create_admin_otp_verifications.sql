-- V001: Create the admin OTP verification table (additive only).
-- Safe to run multiple times. Creates NOTHING else, drops NOTHING, deletes NO data.
-- Matches entity com.visitorgate.domain.AdminOtpVerification exactly
-- (Hibernate ddl-auto=validate passes against this definition).
-- Apply once to the production database, then do not rerun blindly.

CREATE TABLE IF NOT EXISTS `admin_otp_verifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `otp_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `attempt_count` int NOT NULL DEFAULT '0',
  `used` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `fk_otp_user` (`user_id`),
  CONSTRAINT `fk_otp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
