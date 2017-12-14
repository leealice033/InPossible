package com.insnergy.web;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class InPossibleIndexController {
  static final String PATH = "/inpossible";
  
  @GetMapping("/inpossible")
  public String toIndexPage(Model model) {
    log.debug("enter_{}",PATH);
    
    return "inpossible-index";
  }
}
