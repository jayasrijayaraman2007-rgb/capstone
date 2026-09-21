package com.visitorgate.web;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.domain.Visitor;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.VisitRequestRepository;
import com.visitorgate.repo.VisitorRepository;
import com.visitorgate.service.GatePassService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EntryExitControllerTest {

  @Autowired MockMvc mvc;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;
  @Autowired VisitRequestRepository requests;
  @Autowired GatePassService passes;

  private Long approvedPassId;
  private Long pendingPasslessRequestId;

  @BeforeEach
  void setUp() {
    Visitor v = visitors.save(new Visitor("Flow Visitor", "9000000005", null, null, "ID-5"));
    Host h = hosts.save(new Host("Flow Host", "HR", null, "flowhost5@example.com"));
    VisitRequest approved = requests.save(new VisitRequest(v, h, "Meeting"));
    approved.approve();
    approvedPassId = passes.generateFromRequest(approved.getRequestId()).getPassId();
    pendingPasslessRequestId = requests.save(new VisitRequest(v, h, "Tour")).getRequestId();
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void entryThenExitCompletesPass() throws Exception {
    mvc.perform(post("/passes/" + approvedPassId + "/entry").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId));
    mvc.perform(post("/passes/" + approvedPassId + "/entry").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId + "?error=state"));
    mvc.perform(post("/passes/" + approvedPassId + "/exit").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId));
    mvc.perform(post("/passes/" + approvedPassId + "/exit").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId + "?error=state"));
    mvc.perform(get("/history")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void exitBeforeEntryIsRejected() throws Exception {
    VisitRequest r = requests.findById(pendingPasslessRequestId).orElseThrow();
    r.approve();
    Long pid = passes.generateFromRequest(r.getRequestId()).getPassId();
    mvc.perform(post("/passes/" + pid + "/exit").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + pid + "?error=state"));
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotRecordMovementsButSeesOwnHistory() throws Exception {
    mvc.perform(post("/passes/" + approvedPassId + "/entry").with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/history")).andExpect(status().isOk());
  }
}
