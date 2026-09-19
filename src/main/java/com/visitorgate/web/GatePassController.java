package com.visitorgate.web;

import com.visitorgate.domain.GatePass;
import com.visitorgate.domain.PassStatus;
import com.visitorgate.service.EntryExitService;
import com.visitorgate.service.GatePassService;
import org.springframework.http.HttpStatus;
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

  public GatePassController(GatePassService passes, EntryExitService movements) {
    this.passes = passes;
    this.movements = movements;
  }

  @GetMapping
  public String list(@RequestParam(value = "status", required = false) String statusParam, Model model) {
    PassStatus status = parseStatus(statusParam);
    model.addAttribute("passes", passes.list(status));
    model.addAttribute("status", statusParam == null ? "" : statusParam);
    model.addAttribute("counts", passes.statusCounts());
    model.addAttribute("totalPasses", passes.totalCount());
    return "passes/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    GatePass pass = passes.findById(id);
    if (pass == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gate pass not found");
    }
    model.addAttribute("pass", pass);
    model.addAttribute("movement", movements.findByPassId(id));
    return "passes/detail";
  }

  @PostMapping("/{id}/entry")
  public String entry(@PathVariable Long id) {
    try {
      movements.recordEntry(id);
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/passes/" + id + "?error=state";
    }
    return "redirect:/passes/" + id;
  }

  @PostMapping("/{id}/exit")
  public String exit(@PathVariable Long id) {
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
