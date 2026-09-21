package com.visitorgate.security;

import com.visitorgate.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private final UserRepository users;

  public AppUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    com.visitorgate.domain.User user = users.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));
    if (user.getRole() == com.visitorgate.domain.Role.ADMIN) {
      throw new org.springframework.security.authentication.LockedException(
          "Admin sign-in requires email verification");
    }
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        user.getStatus() == com.visitorgate.domain.AccountStatus.ACTIVE,
        true, true, true,
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
  }
}
