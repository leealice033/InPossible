package com.inpossible.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AboutUsController {
  @GetMapping("/about")
  public String toAboutPage(Model model) {
    log.debug("enter_{toAboutPage}");
    
    return "about";
  }
}
