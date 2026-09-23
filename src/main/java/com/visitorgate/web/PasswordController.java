package com.visitorgate.web;

import com.visitorgate.service.PasswordPolicy;
import com.visitorgate.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PasswordController {

  private final UserService userService;

  public PasswordController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/setup")
  public String setupForm(Model model) {
    model.addAttribute("setupForm", new SetupForm());
    model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
    return "setup";
  }

  @PostMapping("/setup")
  public String setup(@Valid @ModelAttribute("setupForm") SetupForm form,
      BindingResult binding, Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
      return "setup";
    }
    try {
      userService.setupPassword(form.getUsername(), form.getEmployeeId(),
          form.getPassword(), form.getConfirmPassword());
    } catch (IllegalArgumentException | IllegalStateException e) {
      binding.reject("setup", e.getMessage());
      model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
      return "setup";
    }
    return "redirect:/login?setup";
  }

  @GetMapping("/account/password")
  public String changeForm(Model model) {
    model.addAttribute("passwordForm", new ChangeForm());
    model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
    return "change-password";
  }

  @PostMapping("/account/password")
  public String change(@Valid @ModelAttribute("passwordForm") ChangeForm form,
      BindingResult binding, Principal principal, Model model) {
    if (binding.hasErrors()) {
      model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
      return "change-password";
    }
    try {
      userService.changePassword(principal == null ? "" : principal.getName(),
          form.getCurrentPassword(), form.getPassword(), form.getConfirmPassword());
    } catch (IllegalArgumentException e) {
      binding.reject("change", e.getMessage());
      model.addAttribute("policy", PasswordPolicy.REQUIREMENTS_TEXT);
      return "change-password";
    }
    model.addAttribute("changed", true);
    model.addAttribute("passwordForm", new ChangeForm());
    return "change-password";
  }

  public static class SetupForm {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Employee ID is required")
    private String employeeId;
    private String password;
    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
  }

  public static class ChangeForm {
    private String currentPassword;
    private String password;
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
  }
}
