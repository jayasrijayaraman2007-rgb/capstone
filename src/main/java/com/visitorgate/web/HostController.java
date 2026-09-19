package com.visitorgate.web;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.service.HostService;
import com.visitorgate.repo.UserRepository;
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
@RequestMapping("/admin/hosts")
public class HostController {

  private final HostService hosts;
  private final UserRepository users;

  public HostController(HostService hosts, UserRepository users) {
    this.hosts = hosts;
    this.users = users;
  }

  @GetMapping
  public String list(@RequestParam(value = "q", required = false) String q, Model model) {
    model.addAttribute("hosts", hosts.search(q));
    model.addAttribute("q", q == null ? "" : q);
    return "admin/hosts/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    model.addAttribute("host", new Host("", "", null, null));
    model.addAttribute("formTitle", "Add host");
    model.addAttribute("formAction", "/admin/hosts");
    model.addAttribute("hostUsers", users.findByRole(Role.HOST));
    model.addAttribute("accountId", "");
    return "admin/hosts/form";
  }

  @PostMapping
  public String create(@Valid @ModelAttribute("host") Host host, BindingResult binding,
      @RequestParam(value = "accountId", required = false) Long accountId, Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("formTitle", "Add host");
      model.addAttribute("formAction", "/admin/hosts");
      model.addAttribute("hostUsers", users.findByRole(Role.HOST));
      model.addAttribute("accountId", accountId == null ? "" : accountId.toString());
      return "admin/hosts/form";
    }
    host.setAccount(resolveAccount(accountId));
    Host saved = hosts.save(host);
    return "redirect:/admin/hosts/" + saved.getHostId();
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    Host host = hosts.findById(id);
    if (host == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found");
    }
    model.addAttribute("host", host);
    return "admin/hosts/detail";
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id, Model model) {
    Host host = hosts.findById(id);
    if (host == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found");
    }
    model.addAttribute("host", host);
    model.addAttribute("formTitle", "Edit host");
    model.addAttribute("formAction", "/admin/hosts/" + id + "/edit");
    model.addAttribute("hostUsers", users.findByRole(Role.HOST));
    model.addAttribute("accountId", host.getAccount() == null ? "" : host.getAccount().getUserId().toString());
    return "admin/hosts/form";
  }

  @PostMapping("/{id}/edit")
  public String update(@PathVariable Long id, @Valid @ModelAttribute("host") Host form,
      BindingResult binding, @RequestParam(value = "accountId", required = false) Long accountId,
      Model model) {
    Host existing = hosts.findById(id);
    if (existing == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found");
    }
    if (binding.hasErrors()) {
      model.addAttribute("formTitle", "Edit host");
      model.addAttribute("formAction", "/admin/hosts/" + id + "/edit");
      model.addAttribute("hostUsers", users.findByRole(Role.HOST));
      model.addAttribute("accountId", accountId == null ? "" : accountId.toString());
      return "admin/hosts/form";
    }
    existing.setName(form.getName());
    existing.setDepartment(form.getDepartment());
    existing.setPhone(form.getPhone());
    existing.setEmail(form.getEmail());
    existing.setAccount(resolveAccount(accountId));
    hosts.save(existing);
    return "redirect:/admin/hosts/" + id;
  }

  private User resolveAccount(Long accountId) {
    if (accountId == null) {
      return null;
    }
    return users.findById(accountId).orElse(null);
  }
}
