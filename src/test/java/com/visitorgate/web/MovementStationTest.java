package com.visitorgate.web;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.domain.Visitor;
import com.visitorgate.repo.HostRepository;
import com.visitorgate.repo.UserRepository;
import com.visitorgate.repo.VisitRequestRepository;
import com.visitorgate.repo.VisitorRepository;
import com.visitorgate.service.GatePassService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovementStationTest {

  @Autowired MockMvc mvc;
  @Autowired VisitorRepository visitors;
  @Autowired HostRepository hosts;
  @Autowired VisitRequestRepository requests;
  @Autowired UserRepository userRepo;
  @Autowired PasswordEncoder encoder;
  @Autowired GatePassService passes;

  private Long approvedPassId;

  @BeforeEach
  void setUp() {
    Visitor v = visitors.save(new Visitor("Station Visitor", "9000000006", null, null, "ID-6"));
    User hu = userRepo.save(new User("Station Host", "station-host", encoder.encode("x"), Role.HOST));
    Host h = new Host("Station Host", "Ops", null, "station@example.com");
    h.setAccount(hu);
    h = hosts.save(h);
    VisitRequest r = requests.save(new VisitRequest(v, h, "Check"));
    r.approve();
    approvedPassId = passes.generateFromRequest(r.getRequestId()).getPassId();
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void entryStationLookupAndRecord() throws Exception {
    mvc.perform(get("/entry")).andExpect(status().isOk());
    mvc.perform(get("/entry").param("passId", "GP-" + approvedPassId)).andExpect(status().isOk());
    mvc.perform(get("/entry").param("passId", "GP-999999")).andExpect(status().isOk());
    mvc.perform(post("/entry/" + approvedPassId).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId));
    mvc.perform(get("/exit").param("passId", String.valueOf(approvedPassId)))
        .andExpect(status().isOk());
    mvc.perform(post("/exit/" + approvedPassId).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/passes/" + approvedPassId));
  }

  @Test
  @WithMockUser(roles = "HOST")
  void hostCannotUseStations() throws Exception {
    mvc.perform(get("/entry")).andExpect(status().isForbidden());
    mvc.perform(get("/exit")).andExpect(status().isForbidden());
    mvc.perform(post("/entry/" + approvedPassId).with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  void anonymousCannotUseStations() throws Exception {
    mvc.perform(get("/entry"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(username = "station-host", roles = "HOST")
  void hostSeesOwnPassesAndHistory() throws Exception {
    mvc.perform(get("/passes")).andExpect(status().isOk());
    mvc.perform(get("/passes/" + approvedPassId)).andExpect(status().isOk());
    mvc.perform(get("/history")).andExpect(status().isOk());
    mvc.perform(post("/passes/" + approvedPassId + "/entry").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "SECURITY_OFFICER")
  void securityDashboardAndSettings() throws Exception {
    mvc.perform(get("/dashboard")).andExpect(status().isOk());
    mvc.perform(get("/settings")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "station-host", roles = "HOST")
  void hostDashboard() throws Exception {
    mvc.perform(get("/dashboard")).andExpect(status().isOk());
  }
}
