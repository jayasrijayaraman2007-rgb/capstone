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
class VisitorControllerTest {

  @Autowired MockMvc mvc;

  @Test
  void anonymousListRedirectsToLogin() throws Exception {
    mvc.perform(get("/visitors"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerSeesListAndForm() throws Exception {
    mvc.perform(get("/visitors")).andExpect(status().isOk());
    mvc.perform(get("/visitors/new")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void createValidRedirectsToDetail() throws Exception {
    mvc.perform(post("/visitors").with(csrf())
            .param("name", "Arun Kumar")
            .param("phone", "9810012345")
            .param("idProof", "Driving Licence TN-01"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/visitors/*"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void createInvalidRedisplaysForm() throws Exception {
    mvc.perform(post("/visitors").with(csrf())
            .param("name", "")
            .param("phone", "9810012345")
            .param("idProof", "PAN"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void missingVisitorIs404() throws Exception {
    mvc.perform(get("/visitors/999999")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotAccessVisitors() throws Exception {
    mvc.perform(get("/visitors")).andExpect(status().isForbidden());
  }
}
