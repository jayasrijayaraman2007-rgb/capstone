package com.visitorgate.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HostControllerTest {

  @Autowired MockMvc mvc;

  @Test
  void anonymousListRedirectsToLogin() throws Exception {
    mvc.perform(get("/admin/hosts"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminSeesListAndForm() throws Exception {
    mvc.perform(get("/admin/hosts")).andExpect(status().isOk());
    mvc.perform(get("/admin/hosts/new")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createValidRedirectsToDetail() throws Exception {
    mvc.perform(post("/admin/hosts").with(csrf())
            .param("name", "Ravi Menon")
            .param("department", "Security")
            .param("email", "ravi@example.com"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/admin/hosts/*"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createInvalidRedisplaysForm() throws Exception {
    mvc.perform(post("/admin/hosts").with(csrf())
            .param("name", "")
            .param("department", "Security"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void missingHostIs404() throws Exception {
    mvc.perform(get("/admin/hosts/999999")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerCannotAccessHosts() throws Exception {
    mvc.perform(get("/admin/hosts")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotAccessHosts() throws Exception {
    mvc.perform(get("/admin/hosts")).andExpect(status().isForbidden());
  }
}
