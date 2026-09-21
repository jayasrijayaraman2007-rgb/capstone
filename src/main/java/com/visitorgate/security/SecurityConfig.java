package com.visitorgate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/error", "/css/**", "/api/health").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/visitors/**").hasAnyRole("ADMIN", "SECURITY_OFFICER")
            .requestMatchers("/passes/**", "/history").hasAnyRole("ADMIN", "SECURITY_OFFICER", "HOST")
            .requestMatchers("/entry", "/entry/**", "/exit", "/exit/**").hasAnyRole("ADMIN", "SECURITY_OFFICER")
            .requestMatchers(HttpMethod.GET, "/requests/new").hasAnyRole("ADMIN", "SECURITY_OFFICER")
            .requestMatchers(HttpMethod.POST, "/requests").hasAnyRole("ADMIN", "SECURITY_OFFICER")
            .requestMatchers(HttpMethod.POST, "/requests/*/pass").hasAnyRole("ADMIN", "SECURITY_OFFICER")
            .requestMatchers("/requests/**").hasAnyRole("HOST", "ADMIN")
            .requestMatchers("/dashboard").authenticated()
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true)
            .permitAll())
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll());
    return http.build();
  }
}
