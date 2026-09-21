package com.visitorgate.web;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import com.visitorgate.service.EntryExitService;
import com.visitorgate.service.GatePassService;
import com.visitorgate.service.VisitRequestService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/passes")
public class GatePassController {

  private final GatePassService passes;
  private final EntryExitService movements;
  private final VisitRequestService requests;

  public GatePassController(GatePassService passes, EntryExitService movements,
      VisitRequestService requests) {
    this.passes = passes;
    this.movements = movements;
    this.requests = requests;
  }

  @GetMapping
  public String list(@RequestParam(value = "status", required = false) String statusParam,
      Model model, Principal principal, Authentication auth) {
    PassStatus status = parseStatus(statusParam);
    List<GatePass> list;
    if (isHost(auth)) {
      list = passes.passesForHost(principal == null ? null : principal.getName(), requests);
      if (status != null) {
        list = list.stream().filter(p -> p.getStatus() == status).toList();
      }
    } else {
      list = passes.list(status);
    }
    model.addAttribute("passes", list);
    model.addAttribute("status", statusParam == null ? "" : statusParam);
    model.addAttribute("counts", passes.statusCounts());
    model.addAttribute("totalPasses", passes.totalCount());
    model.addAttribute("isHost", isHost(auth));
    return "passes/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model, Principal principal, Authentication auth) {
    GatePass pass = passes.findById(id);
    if (pass == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gate pass not found");
    }
    if (isHost(auth) && !ownsPass(pass, principal)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your gate pass");
    }
    model.addAttribute("pass", pass);
    model.addAttribute("movement", movements.findByPassId(id));
    return "passes/detail";
  }

  private boolean isHost(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"))
        && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  private boolean ownsPass(GatePass pass, Principal principal) {
    return principal != null && pass.getHost().getAccount() != null
        && principal.getName().equals(pass.getHost().getAccount().getUsername());
  }

  @PostMapping("/{id}/entry")
  public String entry(@PathVariable Long id, Authentication auth) {
    if (isHost(auth)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hosts cannot record entry");
    }
    try {
      movements.recordEntry(id);
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/passes/" + id + "?error=state";
    }
    return "redirect:/passes/" + id;
  }

  @PostMapping("/{id}/exit")
  public String exit(@PathVariable Long id, Authentication auth) {
    if (isHost(auth)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hosts cannot record exit");
    }
    try {
      movements.recordExit(id);
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/passes/" + id + "?error=state";
    }
    return "redirect:/passes/" + id;
  }

  private PassStatus parseStatus(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return PassStatus.valueOf(raw.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
