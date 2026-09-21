package com.visitorgate.web;

import com.visitorgate.repo.GatePassRepository;
import com.visitorgate.repo.UserRepository;
import com.visitorgate.repo.VisitRequestRepository;
import com.visitorgate.repo.VisitorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsController {

  private final UserRepository users;
  private final VisitorRepository visitors;
  private final VisitRequestRepository requests;
  private final GatePassRepository passes;

  public SettingsController(UserRepository users, VisitorRepository visitors,
      VisitRequestRepository requests, GatePassRepository passes) {
    this.users = users;
    this.visitors = visitors;
    this.requests = requests;
    this.passes = passes;
  }

  @GetMapping
  public String settings(Model model) {
    model.addAttribute("appName", "Visitor Entry & Gate Pass Management System");
    model.addAttribute("javaVersion", System.getProperty("java.version"));
    model.addAttribute("userCount", users.count());
    model.addAttribute("visitorCount", visitors.count());
    model.addAttribute("requestCount", requests.count());
    model.addAttribute("passCount", passes.count());
    return "settings";
  }
}
