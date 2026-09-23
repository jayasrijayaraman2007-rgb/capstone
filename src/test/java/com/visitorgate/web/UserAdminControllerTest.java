package com.visitorgate.web;

import com.visitorgate.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
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
class UserAdminControllerTest {

  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired PasswordEncoder encoder;

  private void createValid(String username, String email, String emp, String role) throws Exception {
    mvc.perform(post("/admin/users").with(csrf())
            .param("name", "Test Person")
            .param("employeeId", emp)
            .param("email", email)
            .param("username", username)
            .param("role", role))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/admin/users/*"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCreatesAccountsWithoutPasswords() throws Exception {
    createValid("newhost1", "newhost1@example.com", "EMP-101", "HOST");
    createValid("newofficer1", "newofficer1@example.com", "EMP-102", "SECURITY_OFFICER");
    assertTrue(users.existsByUsername("newhost1"));
    assertTrue(users.existsByUsername("newofficer1"));
    assertNull(users.findByUsername("newhost1").orElseThrow().getPassword());
    assertEquals(com.visitorgate.domain.AccountStatus.INACTIVE,
        users.findByUsername("newhost1").orElseThrow().getStatus());
    mvc.perform(get("/admin/users")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void userSetsOwnPasswordAfterCreation() throws Exception {
    createValid("selfpw", "selfpw@example.com", "EMP-103", "HOST");
    mvc.perform(formLogin().user("selfpw").password("A1@selfp"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
    mvc.perform(post("/setup").with(csrf())
            .param("username", "selfpw")
            .param("employeeId", "EMP-103")
            .param("password", "A1@selfp")
            .param("confirmPassword", "A1@selfp"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login*"));
    mvc.perform(formLogin().user("selfpw").password("A1@selfp"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void duplicatesAreRejectedWithFriendlyErrors() throws Exception {
    createValid("dupuser", "dup1@example.com", "EMP-201", "HOST");
    mvc.perform(post("/admin/users").with(csrf())
            .param("name", "Dup Two").param("employeeId", "EMP-202")
            .param("email", "dup2@example.com").param("username", "dupuser")
            .param("role", "HOST"))
        .andExpect(status().isOk());
    mvc.perform(post("/admin/users").with(csrf())
            .param("name", "Dup Three").param("employeeId", "EMP-203")
            .param("email", "dup1@example.com").param("username", "freshuser")
            .param("role", "HOST"))
        .andExpect(status().isOk());
    mvc.perform(post("/admin/users").with(csrf())
            .param("name", "Dup Four").param("employeeId", "EMP-201")
            .param("email", "fresh2@example.com").param("username", "freshuser2")
            .param("role", "HOST"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void activateDeactivateAndSelfGuard() throws Exception {
    createValid("statususer", "status@example.com", "EMP-301", "HOST");
    Long id = users.findByUsername("statususer").orElseThrow().getUserId();
    mvc.perform(post("/setup").with(csrf())
            .param("username", "statususer").param("employeeId", "EMP-301")
            .param("password", "A1@stat1").param("confirmPassword", "A1@stat1"))
        .andExpect(status().is3xxRedirection());
    mvc.perform(post("/admin/users/" + id + "/deactivate").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/users/" + id));
    mvc.perform(formLogin().user("statususer").password("A1@stat1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/login?error*"));
    mvc.perform(post("/admin/users/" + id + "/activate").with(csrf()))
        .andExpect(status().is3xxRedirection());
    mvc.perform(formLogin().user("statususer").password("A1@stat1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/dashboard*"));
  }

  @Test
  @WithMockUser(username = "selfadmin", roles = "ADMIN")
  void adminCannotDeactivateSelf() throws Exception {
    com.visitorgate.domain.User self = users.save(new com.visitorgate.domain.User(
        "Self Admin", "selfadmin", encoder.encode("A1@self1"), com.visitorgate.domain.Role.ADMIN));
    mvc.perform(post("/admin/users/" + self.getUserId() + "/deactivate").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/users/" + self.getUserId() + "?error=self"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void securityCannotCreateAccounts() throws Exception {
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    mvc.perform(get("/admin/users/new")).andExpect(status().isForbidden());
    mvc.perform(post("/admin/users").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotCreateAccounts() throws Exception {
    mvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    mvc.perform(post("/admin/users").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  void anonymousCannotReachAdminUsers() throws Exception {
    mvc.perform(get("/admin/users"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }
}
