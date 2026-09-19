package com.visitorgate.web;

import com.visitorgate.service.EntryExitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/history")
public class HistoryController {

  private final EntryExitService movements;

  public HistoryController(EntryExitService movements) {
    this.movements = movements;
  }

  @GetMapping
  public String history(Model model) {
    model.addAttribute("records", movements.history());
    return "history/list";
  }
}
