//package com.insnergy.web;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.insnergy.cofig.InAnalysisConfig;
//import com.insnergy.service.UserInfoService;
//import com.insnergy.vo.ProjectInfo;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
////TODO
//@Controller
//@Slf4j
//public class RegistrationController {
//  
//  private static final String PATH = "/signup";
//  
//  private final InAnalysisConfig config;
//  private final UserInfoService userService;
//  
//  public RegistrationController(InAnalysisConfig config, UserInfoService userService) {
//    this.config = config;
//    this.userService = userService;
//  }
//  
//  @GetMapping(PATH)
//  public String toRegistrationPage(Model model) {
//    model.addAttribute("config", config);
//    
//    return "signup";
//  }
//  
//  @Data
//  @AllArgsConstructor
//  @NoArgsConstructor
//  @Builder
//  public static class CreateAccountParam {
//    String userName;
//    String studentId;
//    String Email;
//    String password;
//    String confirmPassword;
//  }
//  
//  @PostMapping(path = PATH, params = "action=Submit")
//  public String createAccount(CreateAccountParam param, Model model, RedirectAttributes redirectAttributes) {
//    log.debug("CreateAccountParam={}", param);
//    
//    if (param.getConfirmPassword()
//             .equals(param.getPassword())) {
//      
//      userService.addUser(param);
//      // TODO
//      userService.findUserInfoById(param.getStudentId())
//                 .ifPresent(userInfo -> {
//                   
//                   userInfo.getProjects()
//                           .values();
//                   
//                   model.addAttribute("projectList", userInfo.getProjects()
//                                                             .values());
//                 });
//      
//    } else {
//      model.addAttribute("errorMessage", "Please confirm your password again!");
//      return "signup";
//    }
//    model.addAttribute("signUpSuccess", "Sign up Successfully!");
//    return "login";
//    
//  }
//  
//}
