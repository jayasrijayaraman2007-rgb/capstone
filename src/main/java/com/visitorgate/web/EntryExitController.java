package com.visitorgate.web;

import com.visitorgate.domain.GatePass;
import com.visitorgate.service.EntryExitService;
import com.visitorgate.service.GatePassService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EntryExitController {

  private final GatePassService passes;
  private final EntryExitService movements;

  public EntryExitController(GatePassService passes, EntryExitService movements) {
    this.passes = passes;
    this.movements = movements;
  }

  @GetMapping("/entry")
  public String entryPage(@RequestParam(value = "passId", required = false) String rawId, Model model) {
    return lookup("entry", rawId, model);
  }

  @PostMapping("/entry/{id}")
  public String recordEntry(@PathVariable Long id) {
    try {
      movements.recordEntry(id);
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/entry?passId=" + id + "&error=state";
    }
    return "redirect:/passes/" + id;
  }

  @GetMapping("/exit")
  public String exitPage(@RequestParam(value = "passId", required = false) String rawId, Model model) {
    return lookup("exit", rawId, model);
  }

  @PostMapping("/exit/{id}")
  public String recordExit(@PathVariable Long id) {
    try {
      movements.recordExit(id);
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/exit?passId=" + id + "&error=state";
    }
    return "redirect:/passes/" + id;
  }

  private String lookup(String mode, String rawId, Model model) {
    model.addAttribute("mode", mode);
    model.addAttribute("query", rawId == null ? "" : rawId);
    if (rawId == null || rawId.isBlank()) {
      return "movement/station";
    }
    GatePass pass = null;
    Long id = parsePassId(rawId);
    if (id != null) {
      pass = passes.findById(id);
    }
    if (pass == null) {
      model.addAttribute("notFound", true);
      return "movement/station";
    }
    model.addAttribute("pass", pass);
    model.addAttribute("movement", movements.findByPassId(pass.getPassId()));
    return "movement/station";
  }

  private Long parsePassId(String raw) {
    String digits = raw.replaceAll("[^0-9]", "");
    if (digits.isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(digits);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
