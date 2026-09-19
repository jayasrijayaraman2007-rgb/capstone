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
class VisitRequestControllerTest {

  @Autowired MockMvc mvc;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;
  @Autowired VisitRequestRepository requests;

  private Visitor visitor;
  private Host host;
  private Host otherHost;

  @BeforeEach
  void setUp() {
    visitor = visitors.save(new Visitor("Flow Visitor", "9000000002", null, null, "ID-2"));
    host = hosts.save(new Host("Flow Host", "HR", null, "flowhost@example.com"));
    otherHost = hosts.save(new Host("Other Host", "Finance", null, "otherhost@example.com"));
  }

  @Test
  void anonymousListRedirectsToLogin() throws Exception {
    mvc.perform(get("/requests"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void officerCanCreateButCannotListOrDecide() throws Exception {
    mvc.perform(get("/requests/new")).andExpect(status().isOk());
    mvc.perform(post("/requests").with(csrf())
            .param("visitorId", visitor.getVisitorId().toString())
            .param("hostId", host.getHostId().toString())
            .param("purpose", "Delivery"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/requests/*"));
    mvc.perform(get("/requests")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "flowhost@example.com", roles = "HOST")
  void hostSeesOwnRequestsAndApproves() throws Exception {
    VisitRequest r = requests.save(new VisitRequest(visitor, host, "Meeting"));
    mvc.perform(get("/requests")).andExpect(status().isOk());
    mvc.perform(get("/requests/" + r.getRequestId())).andExpect(status().isOk());
    mvc.perform(post("/requests/" + r.getRequestId() + "/approve").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/requests/" + r.getRequestId()));
    mvc.perform(post("/requests/" + r.getRequestId() + "/approve").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/requests/" + r.getRequestId() + "?error=state"));
  }

  @Test
  @WithMockUser(username = "flowhost@example.com", roles = "HOST")
  void hostCannotSeeOthersRequests() throws Exception {
    VisitRequest r = requests.save(new VisitRequest(visitor, otherHost, "Audit"));
    mvc.perform(get("/requests/" + r.getRequestId())).andExpect(status().isForbidden());
    mvc.perform(post("/requests/" + r.getRequestId() + "/reject").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminRejects() throws Exception {
    VisitRequest r = requests.save(new VisitRequest(visitor, host, "Sales visit"));
    mvc.perform(post("/requests/" + r.getRequestId() + "/reject").with(csrf()))
        .andExpect(status().is3xxRedirection());
    mvc.perform(get("/requests?status=REJECTED")).andExpect(status().isOk());
  }
}
