package com.insnergy.web.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.service.UserInfoService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UserManagementController {
  
  private static final String PATH = "/admin/user-management";
  
  private final InAnalysisConfig config;
  private final UserInfoService userService;
  
  public UserManagementController(InAnalysisConfig config, UserInfoService userService) {
    this.config = config;
    this.userService = userService;
  }
  
  @GetMapping(PATH)
  public String index(Model model, Principal principal) {
    final String userId = principal.getName();
    log.debug("index userId={}", userId);
    
    model.addAttribute("config", config);
    model.addAttribute("userList", userService.findAll());
    
    return "user-management";
  }
  
}
