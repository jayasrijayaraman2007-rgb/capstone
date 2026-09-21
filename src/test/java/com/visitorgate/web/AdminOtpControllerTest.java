package com.visitorgate.web;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.UserRepository;
import com.visitorgate.service.AdminMailService;
import com.visitorgate.service.OtpCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminOtpControllerTest {

  @TestConfiguration
  static class OtpTestConfig {
    @Bean
    @Primary
    OtpCodeGenerator generator() {
      return () -> "482731";
    }
  }

  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;
  @MockitoBean AdminMailService mail;

  @BeforeEach
  void setUp() {
    when(mail.isConfigured()).thenReturn(true);
    User admin = new User("Session Admin", "session-admin", encoder.encode("x"), Role.ADMIN);
    admin.setEmail("session-admin@example.com");
    users.save(admin);
  }

  private void requestCode() throws Exception {
    mvc.perform(post("/login/admin").with(csrf()).param("email", "session-admin@example.com"))
        .andExpect(status().isOk());
  }

  private void assertAnonymous(MvcResult result) throws Exception {
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
    var request = get("/dashboard");
    if (session != null) {
      request.session(session);
    }
    mvc.perform(request).andExpect(status().is3xxRedirection());
  }

  @Test
  void unknownEmailCreatesNoSession() throws Exception {
    mvc.perform(post("/login/admin").with(csrf()).param("email", "ghost@example.com"))
        .andExpect(status().isOk());
    MvcResult result = mvc.perform(post("/login/admin/verify").with(csrf())
            .param("email", "ghost@example.com")
            .param("code", "482731"))
        .andExpect(status().isOk())
        .andReturn();
    assertAnonymous(result);
  }

  @Test
  void correctOtpCreatesAdminSession() throws Exception {
    requestCode();
    MvcResult result = mvc.perform(post("/login/admin/verify").with(csrf())
            .param("email", "session-admin@example.com")
            .param("code", "482731"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dashboard?verified"))
        .andReturn();
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
    mvc.perform(get("/admin/users").session(session)).andExpect(status().isOk());
  }

  @Test
  void wrongOtpCreatesNoSession() throws Exception {
    requestCode();
    MvcResult result = mvc.perform(post("/login/admin/verify").with(csrf())
            .param("email", "session-admin@example.com")
            .param("code", "000000"))
        .andExpect(status().isOk())
        .andReturn();
    assertAnonymous(result);
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotOpenAdminDashboard() throws Exception {
    mvc.perform(get("/dashboard")).andExpect(status().isOk());
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerCannotOpenAdminDashboard() throws Exception {
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
  }

  @Test
  void adminPasswordLoginIsBlocked() throws Exception {
    mvc.perform(post("/login").with(csrf())
            .param("username", "session-admin")
            .param("password", "x"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
  }
}
