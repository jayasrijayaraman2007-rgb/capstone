package com.visitorgate.web;

import com.visitorgate.domain.Role;
import com.visitorgate.domain.User;
import com.visitorgate.repo.UserRepository;
import com.visitorgate.service.AdminOtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login/admin")
public class AdminOtpController {

  private final AdminOtpService otpService;
  private final UserRepository users;
  private final SecurityContextRepository contextRepository =
      new HttpSessionSecurityContextRepository();

  public AdminOtpController(AdminOtpService otpService, UserRepository users) {
    this.otpService = otpService;
    this.users = users;
  }

  @GetMapping
  public String emailForm() {
    return "admin-login";
  }

  @PostMapping
  public String requestCode(@RequestParam(value = "email", required = false) String email,
      Model model) {
    AdminOtpService.RequestResult result = otpService.requestCode(email);
    model.addAttribute("email", AdminOtpService.normalize(email));
    return switch (result) {
      case SENT -> {
        model.addAttribute("sent", true);
        yield "admin-verify";
      }
      case NOT_CONFIGURED -> {
        model.addAttribute("mailError", true);
        yield "admin-login";
      }
      case COOLDOWN, RATE_LIMITED -> {
        model.addAttribute("rateError", true);
        yield "admin-login";
      }
      case UNKNOWN -> {
        model.addAttribute("sent", true);
        model.addAttribute("generic", true);
        yield "admin-verify";
      }
    };
  }

  @GetMapping("/verify")
  public String verifyForm(@RequestParam(value = "email", required = false) String email,
      Model model) {
    model.addAttribute("email", AdminOtpService.normalize(email));
    return "admin-verify";
  }

  @PostMapping("/verify")
  public String verify(@RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "code", required = false) String code,
      Model model, HttpServletRequest request, HttpServletResponse response) {
    String cleanEmail = AdminOtpService.normalize(email);
    AdminOtpService.VerifyResult result = otpService.verifyCode(cleanEmail, code);
    model.addAttribute("email", cleanEmail);
    if (result == AdminOtpService.VerifyResult.OK) {
      User admin = users.findAll().stream()
          .filter(u -> u.getRole() == Role.ADMIN && cleanEmail.equalsIgnoreCase(u.getEmail()))
          .findFirst()
          .orElse(null);
      if (admin == null) {
        model.addAttribute("invalid", true);
        return "admin-verify";
      }
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(admin.getUsername(), null,
              List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      contextRepository.saveContext(context, request, response);
      return "redirect:/dashboard?verified";
    }
    model.addAttribute(switch (result) {
      case EXPIRED -> "expired";
      case LOCKED -> "locked";
      default -> "invalid";
    }, true);
    return "admin-verify";
  }

  @PostMapping("/resend")
  public String resend(@RequestParam(value = "email", required = false) String email,
      Model model) {
    return requestCode(email, model);
  }
}
