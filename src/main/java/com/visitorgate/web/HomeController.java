package com.visitorgate.web;

import com.visitorgate.service.DashboardService;
import com.visitorgate.service.DashboardService.DashboardStats;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class HomeController {

  private final DashboardService dashboard;

  public HomeController(DashboardService dashboard) {
    this.dashboard = dashboard;
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
    return "dashboard";
  }
}
