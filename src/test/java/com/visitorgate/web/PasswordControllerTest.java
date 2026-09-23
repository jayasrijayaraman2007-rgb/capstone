package com.visitorgate.web;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordControllerTest {

  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;

  @BeforeEach
  void setUp() {
    User pending = new User("Setup Person", "setup-user", null, Role.HOST);
    pending.setEmployeeId("EMP-900");
    users.save(pending);
    User active = new User("Active Person", "active-user", encoder.encode("A1@oldpw"), Role.SECURITY_OFFICER);
    active.setEmployeeId("EMP-901");
    users.save(active);
  }

  @Test
  void setupPageIsPublic() throws Exception {
    mvc.perform(get("/setup")).andExpect(status().isOk());
  }

  @Test
  void setupSuccessActivatesAndAllowsLogin() throws Exception {
    mvc.perform(post("/setup").with(csrf())
            .param("username", "setup-user")
            .param("employeeId", "EMP-900")
            .param("password", "N2@newpw")
            .param("confirmPassword", "N2@newpw"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login*"));
    assertTrue(encoder.matches("N2@newpw",
        users.findByUsername("setup-user").orElseThrow().getPassword()));
    mvc.perform(formLogin().user("setup-user").password("N2@newpw"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
  }

  @Test
  void setupRejectsMismatchWeakUnknownAndMismatchEmployee() throws Exception {
    mvc.perform(post("/setup").with(csrf())
            .param("username", "setup-user").param("employeeId", "EMP-900")
            .param("password", "N2@newpw").param("confirmPassword", "N2@other"))
        .andExpect(status().isOk());
    mvc.perform(post("/setup").with(csrf())
            .param("username", "setup-user").param("employeeId", "EMP-900")
            .param("password", "weakpass").param("confirmPassword", "weakpass"))
        .andExpect(status().isOk());
    mvc.perform(post("/setup").with(csrf())
            .param("username", "ghost").param("employeeId", "EMP-900")
            .param("password", "N2@newpw").param("confirmPassword", "N2@newpw"))
        .andExpect(status().isOk());
    mvc.perform(post("/setup").with(csrf())
            .param("username", "setup-user").param("employeeId", "WRONG")
            .param("password", "N2@newpw").param("confirmPassword", "N2@newpw"))
        .andExpect(status().isOk());
    mvc.perform(formLogin().user("setup-user").password("N2@newpw"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
  }

  @Test
  void setupRefusedWhenPasswordAlreadySet() throws Exception {
    mvc.perform(post("/setup").with(csrf())
            .param("username", "active-user").param("employeeId", "EMP-901")
            .param("password", "N2@newpw").param("confirmPassword", "N2@newpw"))
        .andExpect(status().isOk());
    mvc.perform(formLogin().user("active-user").password("A1@oldpw"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
  }

  @Test
  void changePageRequiresLogin() throws Exception {
    mvc.perform(get("/account/password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(username = "active-user", roles = "SECURITY_OFFICER")
  void changeSuccessWrongCurrentAndMismatch() throws Exception {
    mvc.perform(get("/account/password")).andExpect(status().isOk());
    mvc.perform(post("/account/password").with(csrf())
            .param("currentPassword", "A1@oldpw")
            .param("password", "B2#newpw")
            .param("confirmPassword", "B2#newpw"))
        .andExpect(status().isOk());
    assertTrue(encoder.matches("B2#newpw",
        users.findByUsername("active-user").orElseThrow().getPassword()));
    mvc.perform(post("/account/password").with(csrf())
            .param("currentPassword", "nope")
            .param("password", "C3#newpw")
            .param("confirmPassword", "C3#newpw"))
        .andExpect(status().isOk());
    mvc.perform(post("/account/password").with(csrf())
            .param("currentPassword", "B2#newpw")
            .param("password", "C3#newpw")
            .param("confirmPassword", "C3#other"))
        .andExpect(status().isOk());
    assertTrue(encoder.matches("B2#newpw",
        users.findByUsername("active-user").orElseThrow().getPassword()));
  }

  @Test
  void storedPasswordIsHashNotPlaintext() throws Exception {
    mvc.perform(post("/setup").with(csrf())
            .param("username", "setup-user").param("employeeId", "EMP-900")
            .param("password", "N2@newpw").param("confirmPassword", "N2@newpw"))
        .andExpect(status().is3xxRedirection());
    String stored = users.findByUsername("setup-user").orElseThrow().getPassword();
    assertTrue(!stored.equals("N2@newpw") && stored.startsWith("$2"));
  }

  @Test
  void adminPasswordLoginWorks() throws Exception {
    User admin = new User("Pw Admin", "pw-admin", encoder.encode("A1@admn1"), Role.ADMIN);
    users.save(admin);
    mvc.perform(formLogin().user("pw-admin").password("A1@admn1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
    mvc.perform(formLogin().user("pw-admin").password("wrong"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
  }
}
