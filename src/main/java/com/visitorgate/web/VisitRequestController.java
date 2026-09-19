package com.visitorgate.web;

import com.visitorgate.domain.Host;
import com.visitorgate.domain.RequestStatus;
import com.visitorgate.domain.VisitRequest;
import com.visitorgate.domain.Visitor;
import com.visitorgate.service.HostService;
import com.visitorgate.service.VisitRequestService;
import com.visitorgate.service.VisitorService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/requests")
public class VisitRequestController {

  private final VisitRequestService requests;
  private final VisitorService visitors;
  private final HostService hosts;

  public VisitRequestController(VisitRequestService requests, VisitorService visitors, HostService hosts) {
    this.requests = requests;
    this.visitors = visitors;
    this.hosts = hosts;
  }

  private boolean isAdmin(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  @GetMapping
  public String list(@RequestParam(value = "status", required = false) String statusParam,
      Model model, Principal principal, Authentication auth) {
    RequestStatus status = parseStatus(statusParam);
    List<VisitRequest> list = isAdmin(auth)
        ? requests.listForAdmin(status)
        : requests.listForHostAccount(principal == null ? null : principal.getName(), status);
    model.addAttribute("requests", list);
    model.addAttribute("status", statusParam == null ? "" : statusParam);
    model.addAttribute("isAdmin", isAdmin(auth));
    return "requests/list";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    model.addAttribute("visitors", visitors.search(null));
    model.addAttribute("hosts", hosts.findAll());
    model.addAttribute("requestForm", new RequestForm());
    return "requests/form";
  }

  @PostMapping
  public String create(@Valid @ModelAttribute("requestForm") RequestForm form, BindingResult binding, Model model) {
    Visitor visitor = form.getVisitorId() == null ? null : visitors.findById(form.getVisitorId());
    Host host = form.getHostId() == null ? null : hosts.findById(form.getHostId());
    if (visitor == null) {
      binding.rejectValue("visitorId", "required", "Visitor is required");
    }
    if (host == null) {
      binding.rejectValue("hostId", "required", "Host is required");
    }
    if (binding.hasErrors()) {
      model.addAttribute("visitors", visitors.search(null));
      model.addAttribute("hosts", hosts.findAll());
      return "requests/form";
    }
    VisitRequest saved = requests.save(new VisitRequest(visitor, host, form.getPurpose()));
    return "redirect:/requests/" + saved.getRequestId();
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model, Principal principal, Authentication auth) {
    VisitRequest request = requests.findById(id);
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
    }
    if (!isAdmin(auth) && !ownsRequest(request, principal)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
    }
    model.addAttribute("visitRequest", request);
    return "requests/detail";
  }

  @PostMapping("/{id}/approve")
  public String approve(@PathVariable Long id, Principal principal, Authentication auth) {
    checkDecisionRights(id, principal, auth);
    try {
      requests.approve(id);
    } catch (IllegalStateException e) {
      return "redirect:/requests/" + id + "?error=state";
    }
    return "redirect:/requests/" + id;
  }

  @PostMapping("/{id}/reject")
  public String reject(@PathVariable Long id, Principal principal, Authentication auth) {
    checkDecisionRights(id, principal, auth);
    try {
      requests.reject(id);
    } catch (IllegalStateException e) {
      return "redirect:/requests/" + id + "?error=state";
    }
    return "redirect:/requests/" + id;
  }

  private void checkDecisionRights(Long id, Principal principal, Authentication auth) {
    VisitRequest request = requests.findById(id);
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
    }
    if (!isAdmin(auth) && !ownsRequest(request, principal)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
    }
  }

  private boolean ownsRequest(VisitRequest request, Principal principal) {
    return principal != null && request.getHost().getEmail() != null
        && request.getHost().getEmail().equalsIgnoreCase(principal.getName());
  }

  private RequestStatus parseStatus(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return RequestStatus.valueOf(raw.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static class RequestForm {
    private Long visitorId;
    private Long hostId;
    @jakarta.validation.constraints.NotBlank(message = "Purpose is required")
    private String purpose;

    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long visitorId) { this.visitorId = visitorId; }
    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
  }
}
