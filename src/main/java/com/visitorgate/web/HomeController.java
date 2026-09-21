package com.visitorgate.web;

import com.visitorgate.domain.PassStatus;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.service.DashboardService;
import com.visitorgate.service.DashboardService.DashboardStats;
import com.visitorgate.service.GatePassService;
import com.visitorgate.service.VisitRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class HomeController {

  private final DashboardService dashboard;
  private final GatePassService passes;
  private final VisitRequestService requests;

  public HomeController(DashboardService dashboard, GatePassService passes,
      VisitRequestService requests) {
    this.dashboard = dashboard;
    this.passes = passes;
    this.requests = requests;
  }

  @GetMapping("/api/health")
  @ResponseBody
  public String health() {
    return "OK";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model, Principal principal) {
    DashboardStats stats = dashboard.stats();
    model.addAttribute("username", principal == null ? "" : principal.getName());
    model.addAttribute("totalVisitors", stats.totalVisitors());
    model.addAttribute("pendingRequests", stats.pendingRequests());
    model.addAttribute("approvedRequests", stats.approvedRequests());
    model.addAttribute("insideNow", stats.insideNow());
    model.addAttribute("completedVisits", stats.completedVisits());
    model.addAttribute("recentPasses", stats.recentPasses());
    model.addAttribute("recentRequests", stats.recentRequests());
    model.addAttribute("insidePasses", passes.list(PassStatus.ACTIVE));
    String username = principal == null ? "" : principal.getName();
    model.addAttribute("myPending",
        requests.listForHostAccount(username, RequestStatus.PENDING).size());
    model.addAttribute("myApproved",
        requests.listForHostAccount(username, RequestStatus.APPROVED).size());
    model.addAttribute("myRejected",
        requests.listForHostAccount(username, RequestStatus.REJECTED).size());
    return "dashboard";
  }
}
