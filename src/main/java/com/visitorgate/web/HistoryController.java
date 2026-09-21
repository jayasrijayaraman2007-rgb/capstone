package com.visitorgate.web;

import com.visitorgate.domain.EntryExit;
import com.visitorgate.domain.GatePass;
import com.visitorgate.service.EntryExitService;
import com.visitorgate.service.GatePassService;
import com.visitorgate.service.VisitRequestService;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/history")
public class HistoryController {

  private final EntryExitService movements;
  private final GatePassService passes;
  private final VisitRequestService requests;

  public HistoryController(EntryExitService movements, GatePassService passes,
      VisitRequestService requests) {
    this.movements = movements;
    this.passes = passes;
    this.requests = requests;
  }

  @GetMapping
  public String history(@RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "status", required = false) String statusParam,
      Model model, Principal principal, Authentication auth) {
    List<EntryExit> records;
    if (isHost(auth)) {
      List<GatePass> mine = passes.passesForHost(principal == null ? null : principal.getName(), requests);
      Set<Long> ids = new HashSet<>();
      mine.forEach(p -> ids.add(p.getPassId()));
      records = movements.history().stream().filter(r -> ids.contains(r.getPass().getPassId())).toList();
    } else {
      records = movements.history();
    }
    records = filter(records, q, statusParam);
    model.addAttribute("records", records);
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("status", statusParam == null ? "" : statusParam);
    model.addAttribute("isHost", isHost(auth));
    return "history/list";
  }

  private List<EntryExit> filter(List<EntryExit> records, String q, String statusParam) {
    String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    return records.stream()
        .filter(r -> statusParam == null || statusParam.isBlank()
            || r.getPass().getStatus().name().equalsIgnoreCase(statusParam.trim()))
        .filter(r -> needle.isEmpty()
            || contains(r.getPass().getVisitor().getName(), needle)
            || contains(r.getPass().getVisitor().getPhone(), needle)
            || contains(r.getPass().getHost().getName(), needle)
            || contains("GP-" + r.getPass().getPassId(), needle)
            || contains(String.valueOf(r.getPass().getPassId()), needle))
        .toList();
  }

  private boolean contains(String value, String needle) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
  }

  private boolean isHost(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"))
        && auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
