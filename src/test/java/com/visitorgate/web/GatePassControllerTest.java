package com.visitorgate.web;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.domain.Visitor;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.VisitRequestRepository;
import com.visitorgate.repo.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GatePassControllerTest {

  @Autowired MockMvc mvc;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;
  @Autowired VisitRequestRepository requests;

  private Visitor visitor;
  private Host host;

  @BeforeEach
  void setUp() {
    visitor = visitors.save(new Visitor("Gate Visitor", "9000000004", null, null, "ID-4"));
    host = hosts.save(new Host("Gate Host", "Lobby", null, "gatehost@example.com"));
  }

  @Test
  void anonymousPassListRedirectsToLogin() throws Exception {
    mvc.perform(get("/passes"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerGeneratesPassForApprovedRequest() throws Exception {
    VisitRequest r = requests.save(new VisitRequest(visitor, host, "Visit"));
    r.approve();
    String first = mvc.perform(post("/requests/" + r.getRequestId() + "/pass").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andReturn().getResponse().getRedirectedUrl();
    mvc.perform(post("/requests/" + r.getRequestId() + "/pass").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(first));
    mvc.perform(get("/passes")).andExpect(status().isOk());
    mvc.perform(get(first)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerCannotGenerateForPendingRequest() throws Exception {
    VisitRequest r = requests.save(new VisitRequest(visitor, host, "Visit"));
    mvc.perform(post("/requests/" + r.getRequestId() + "/pass").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/requests/" + r.getRequestId() + "?error=state"));
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostSeesOnlyOwnPasses() throws Exception {
    mvc.perform(get("/passes")).andExpect(status().isOk());
    mvc.perform(get("/passes/999999")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void missingPassIs404() throws Exception {
    mvc.perform(get("/passes/999999")).andExpect(status().isNotFound());
  }
}
