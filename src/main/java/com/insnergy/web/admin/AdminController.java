package com.insnergy.web.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.insnergy.cofig.InAnalysisConfig;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AdminController {
  
  private static final String PATH = "/admin";
  
  private final InAnalysisConfig config;
  
  public AdminController(InAnalysisConfig config) {
    this.config = config;
  }
  
  @GetMapping(PATH)
  public String index(Model model, Principal principal) {
    final String userId = principal.getName();
    log.debug("index userId={}", userId);
    
    model.addAttribute("config", config);
    
    return "admin";
  }
  
}
