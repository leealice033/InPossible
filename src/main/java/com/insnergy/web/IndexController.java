package com.insnergy.web;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.insnergy.service.ServerStatusCache;
import com.insnergy.service.UserInfoService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class IndexController {
  
  private final UserInfoService userService;
  private final ServerStatusCache serverStatusCache;
  static final String defaultUser ="alice";
  static final String defaultProjectId = "";
  static final String defaultType = "regression";
  
  public IndexController(UserInfoService userService, ServerStatusCache serverStatusCache) {
    this.userService = userService;
    this.serverStatusCache = serverStatusCache;
  }
  
  @ModelAttribute("pythonServerStatus")
  public Boolean populatePythonServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.PYTHON);
  }
  
  @ModelAttribute("rServerStatus")
  public Boolean populateRServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.R);
  }
  
  @GetMapping("/refresh")
  public String refreshCsvFileInfo(Model model, Principal principal) {
    log.debug("refreshCsvFileInfo");
    // TODO refresh user project
    return "redirect:/";
  }
  
  @GetMapping("/")
  public String index(Model model, Principal principal) {
    final String userId = defaultUser;
    
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    log.debug("index[{}]={}", userId, _user);
    
    _user.ifPresent(userInfo -> {
      model.addAttribute("userInfo", userInfo);
      log.debug("Index Show Userinfo={}", userInfo);
      
      model.addAttribute("projectTotal", userInfo.getProjects()
                                                 .values()
                                                 .size());
      
      model.addAttribute("apiTotal", userInfo.getApis()
                                             .values()
                                             .size());
      
      log.debug("projectTotal={},apiTotal={}", userInfo.getProjects()
                                                       .values()
                                                       .size(),
          userInfo.getApis()
                  .values()
                  .size());
      
      model.addAttribute("projects", userInfo.getProjects()
                                             .values());
      model.addAttribute("apis", userInfo.getApis()
                                         .values());
    });
    
    return "index";
  }
  
  
  
}
