package com.visitorgate.service;

import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository users;
  private final HostRepository hosts;

  public UserService(UserRepository users, HostRepository hosts) {
    this.users = users;
    this.hosts = hosts;
  }

  @Transactional
  public void deleteUser(Long userId) {
    if (!hosts.findByAccountUserId(userId).isEmpty()) {
      throw new IllegalStateException("User is linked to a host profile and cannot be deleted");
    }
    users.deleteById(userId);
  }
}
