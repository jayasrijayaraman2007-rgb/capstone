package com.visitorgate.web;

import com.visitorgate.domain.Visitor;
import com.visitorgate.service.VisitorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/visitors")
public class VisitorController {

  private final VisitorService visitors;

  public VisitorController(VisitorService visitors) {
    this.visitors = visitors;
  }

  @GetMapping
  public String list(@RequestParam(value = "q", required = false) String q, Model model) {
    model.addAttribute("visitors", visitors.search(q));
    model.addAttribute("q", q == null ? "" : q);
    return "visitors/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    model.addAttribute("visitor", new Visitor("", "", null, null, ""));
    model.addAttribute("formTitle", "Register visitor");
    model.addAttribute("formAction", "/visitors");
    return "visitors/form";
  }

  @PostMapping
  public String create(@Valid @ModelAttribute("visitor") Visitor visitor, BindingResult binding, Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("formTitle", "Register visitor");
      model.addAttribute("formAction", "/visitors");
      return "visitors/form";
    }
    Visitor saved = visitors.save(visitor);
    return "redirect:/visitors/" + saved.getVisitorId();
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    Visitor visitor = visitors.findById(id);
    if (visitor == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found");
    }
    model.addAttribute("visitor", visitor);
    return "visitors/detail";
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id, Model model) {
    Visitor visitor = visitors.findById(id);
    if (visitor == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found");
    }
    model.addAttribute("visitor", visitor);
    model.addAttribute("formTitle", "Edit visitor");
    model.addAttribute("formAction", "/visitors/" + id + "/edit");
    return "visitors/form";
  }

  @PostMapping("/{id}/edit")
  public String update(@PathVariable Long id, @Valid @ModelAttribute("visitor") Visitor form,
      BindingResult binding, Model model) {
    Visitor existing = visitors.findById(id);
    if (existing == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found");
    }
    if (binding.hasErrors()) {
      model.addAttribute("formTitle", "Edit visitor");
      model.addAttribute("formAction", "/visitors/" + id + "/edit");
      return "visitors/form";
    }
    existing.setName(form.getName());
    existing.setPhone(form.getPhone());
    existing.setEmail(form.getEmail());
    existing.setAddress(form.getAddress());
    existing.setIdProof(form.getIdProof());
    visitors.save(existing);
    return "redirect:/visitors/" + id;
  }
}
