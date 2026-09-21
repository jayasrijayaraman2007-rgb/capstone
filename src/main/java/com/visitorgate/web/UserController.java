package com.visitorgate.web;

import com.visitorgate.domain.AccountStatus;
import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
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
@RequestMapping("/admin/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("users", userService.listAll());
    return "admin/users/list";
  }

  @GetMapping("/new")
  public String chooseRole(@RequestParam(value = "type", required = false) String type, Model model) {
    if (type == null) {
      return "admin/users/new";
    }
    Role role = parseRole(type);
    if (role == null) {
      return "redirect:/admin/users/new";
    }
    AccountForm form = new AccountForm();
    form.setRole(role.name());
    form.setStatus(AccountStatus.ACTIVE.name());
    model.addAttribute("accountForm", form);
    model.addAttribute("roleTitle", role == Role.HOST ? "Employee / Host" : "Security Officer");
    model.addAttribute("formAction", "/admin/users");
    model.addAttribute("isEdit", false);
    return "admin/users/form";
  }

  @PostMapping
  public String create(@Valid @ModelAttribute("accountForm") AccountForm form,
      BindingResult binding, Model model) {
    Role role = parseRole(form.getRole());
    if (role == null) {
      return "redirect:/admin/users/new";
    }
    validatePasswords(form, binding, false);
    if (binding.hasErrors()) {
      refillForm(model, role);
      return "admin/users/form";
    }
    try {
      AccountStatus status = parseStatus(form.getStatus());
      User saved = userService.createAccount(form.getName(), form.getEmployeeId(),
          form.getEmail(), form.getPhone(), form.getDepartment(), form.getDesignation(),
          form.getUsername(), form.getPassword(), role, status);
      return "redirect:/admin/users/" + saved.getUserId() + "?created";
    } catch (IllegalArgumentException e) {
      binding.reject("account", e.getMessage());
      refillForm(model, role);
      return "admin/users/form";
    }
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model) {
    User user = userService.findById(id);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    model.addAttribute("user", user);
    return "admin/users/detail";
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable Long id, Model model) {
    User user = userService.findById(id);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    AccountForm form = new AccountForm();
    form.setRole(user.getRole().name());
    form.setName(user.getName());
    form.setEmployeeId(user.getEmployeeId());
    form.setEmail(user.getEmail());
    form.setPhone(user.getPhone());
    form.setDepartment(user.getDepartment());
    form.setDesignation(user.getDesignation());
    form.setUsername(user.getUsername());
    model.addAttribute("accountForm", form);
    model.addAttribute("roleTitle", user.getRole() == Role.HOST ? "Employee / Host" : "Security Officer");
    model.addAttribute("formAction", "/admin/users/" + id + "/edit");
    model.addAttribute("isEdit", true);
    return "admin/users/form";
  }

  @PostMapping("/{id}/edit")
  public String update(@PathVariable Long id, @Valid @ModelAttribute("accountForm") AccountForm form,
      BindingResult binding, Model model) {
    User user = userService.findById(id);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    if (binding.hasErrors()) {
      model.addAttribute("roleTitle", user.getRole() == Role.HOST ? "Employee / Host" : "Security Officer");
      model.addAttribute("formAction", "/admin/users/" + id + "/edit");
      model.addAttribute("isEdit", true);
      return "admin/users/form";
    }
    try {
      userService.updateProfile(id, form.getName(), form.getEmployeeId(), form.getEmail(),
          form.getPhone(), form.getDepartment(), form.getDesignation());
    } catch (IllegalArgumentException e) {
      binding.reject("account", e.getMessage());
      model.addAttribute("roleTitle", user.getRole() == Role.HOST ? "Employee / Host" : "Security Officer");
      model.addAttribute("formAction", "/admin/users/" + id + "/edit");
      model.addAttribute("isEdit", true);
      return "admin/users/form";
    }
    return "redirect:/admin/users/" + id;
  }

  @PostMapping("/{id}/activate")
  public String activate(@PathVariable Long id, Principal principal) {
    try {
      userService.setStatus(id, AccountStatus.ACTIVE, principal == null ? null : principal.getName());
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/admin/users/" + id + "?error=self";
    }
    return "redirect:/admin/users/" + id;
  }

  @PostMapping("/{id}/deactivate")
  public String deactivate(@PathVariable Long id, Principal principal) {
    try {
      userService.setStatus(id, AccountStatus.INACTIVE, principal == null ? null : principal.getName());
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "redirect:/admin/users/" + id + "?error=self";
    }
    return "redirect:/admin/users/" + id;
  }

  private void refillForm(Model model, Role role) {
    model.addAttribute("roleTitle", role == Role.HOST ? "Employee / Host" : "Security Officer");
    model.addAttribute("formAction", "/admin/users");
    model.addAttribute("isEdit", false);
  }

  private void validatePasswords(AccountForm form, BindingResult binding, boolean isEdit) {
    if (isEdit) {
      return;
    }
    if (form.getPassword() == null || form.getPassword().isBlank()) {
      binding.rejectValue("password", "required", "Password is required");
    } else if (!form.getPassword().equals(form.getConfirmPassword())) {
      binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
    }
  }

  private Role parseRole(String raw) {
    if ("host".equalsIgnoreCase(raw) || "HOST".equals(raw)) {
      return Role.HOST;
    }
    if ("security".equalsIgnoreCase(raw) || "SECURITY_OFFICER".equals(raw)) {
      return Role.SECURITY_OFFICER;
    }
    return null;
  }

  private AccountStatus parseStatus(String raw) {
    if ("INACTIVE".equalsIgnoreCase(raw)) {
      return AccountStatus.INACTIVE;
    }
    return AccountStatus.ACTIVE;
  }

  public static class AccountForm {
    private String role;
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String name;
    @NotBlank(message = "Employee ID is required")
    @Size(max = 50)
    private String employeeId;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100)
    private String email;
    @Size(max = 20)
    private String phone;
    @Size(max = 100)
    private String department;
    @Size(max = 100)
    private String designation;
    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String username;
    private String password;
    private String confirmPassword;
    private String status;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
  }
}
