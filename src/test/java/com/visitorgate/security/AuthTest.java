package com.visitorgate.security;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthTest {

  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;

  @BeforeEach
  void setUp() {
    users.deleteAll();
    users.save(new User("Security Officer", "officer1", encoder.encode("officer-pass"), Role.SECURITY_OFFICER));
  }

  @Test
  void anonymousDashboardRedirectsToLogin() throws Exception {
    mvc.perform(get("/dashboard"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void loginPageIsPublic() throws Exception {
    mvc.perform(get("/login")).andExpect(status().isOk());
  }

  @Test
  void validCredentialsReachDashboard() throws Exception {
    mvc.perform(formLogin().user("officer1").password("officer-pass"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
  }

  @Test
  void invalidCredentialsStayAtLoginWithError() throws Exception {
    mvc.perform(formLogin().user("officer1").password("wrong-pass"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
  }
}
