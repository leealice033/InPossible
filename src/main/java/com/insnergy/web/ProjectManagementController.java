package com.insnergy.web;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.insnergy.repo.ProjectEntityRepo;
import com.insnergy.repo.UserEntityRepo;
import com.insnergy.service.ProjectInfoService;
import com.insnergy.service.ServerStatusCache;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.ModelService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.ProjectType;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ProjectManagementController {
  
  private static final String CREATE_PROJECT_PATH = "/create-project";
  private static final String DELETE_PROJECT_PATH = "/delete-project";
  private static final String UPDATE_PROJECT_PATH = "/project-management";
  
  private static final String DEFAULT_PROJECT_NAME = "Untitled Project";
  
  private final UserInfoService userService;
  private final ServerStatusCache serverStatusCache;
  private final ProjectInfoService projectService;
  private final CsvService csvService;
  private final ModelService modelService;
  private final ProjectEntityRepo projectRepo;
  
  public ProjectManagementController(UserInfoService userService, ServerStatusCache serverStatusCache,
      ProjectInfoService projectService, CsvService csvService, ModelService modelService,
      ProjectEntityRepo projectRepo) {
    this.userService = userService;
    this.serverStatusCache = serverStatusCache;
    this.projectService = projectService;
    this.csvService = csvService;
    this.modelService = modelService;
    this.projectRepo = projectRepo;
  }
  
  @ModelAttribute("pythonServerStatus")
  public Boolean populatePythonServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.PYTHON);
  }
  
  @ModelAttribute("rServerStatus")
  public Boolean populateRServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.R);
  }
  
  @GetMapping("/dashboard")
  public String dashboard(Model model, Principal principal) {
    final String userId = principal.getName();
    
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    log.debug("index[{}]={}", userId, _user);
    
    _user.ifPresent(userInfo -> {
      
      Collection<ProjectInfo> _projectList = userInfo.getProjects()
                                                     .values();
      _projectList.iterator()
                  .forEachRemaining(projectInfo -> {
                    userService.refreshUserProject(userId, projectInfo.getId());
                  });
                  
      log.debug("collection__projectList={}", _projectList);
      // userService.refreshUserProject(userId, userInfo.getProjects().);
      model.addAttribute("projectList", userInfo.getProjects()
                                                .values());
    });
    
    CreateProjectParam defaultParam = CreateProjectParam.builder()
                                                        .projectName(DEFAULT_PROJECT_NAME)
                                                        .projectType(ProjectType.ABNORMAL_DETECTION)
                                                        .build();
    model.addAttribute("createProjectParam", defaultParam);
    
    return "project-management";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class CreateProjectParam {
    String projectName;
    String projectType;
  }
  
  @PostMapping(path = CREATE_PROJECT_PATH, params = "action=Create Project")
  public String doCreateProject(CreateProjectParam param, Model model, Principal principal) {
    String userId = principal.getName();
    log.debug("doCreateProject,CreateProjectParam={}",param);
    log.debug("{}action=Create Project userId={}", CREATE_PROJECT_PATH, userId);
    
    projectService.buildProjectInfo(param)
                  .ifPresent(result -> {
                    log.debug("{}action=doCreateProject_ projectService.buildProjectInfo={}", CREATE_PROJECT_PATH,
                        result);
                    final ProjectInfo projectInfo = result;
                    log.debug("{}action=doCreateProject_ projectInfo={}", CREATE_PROJECT_PATH, projectInfo);
                    projectService.addProject(userId, projectInfo);
                    log.debug("after_call_projctService_addProjec={}", projectInfo);
                    
                    userService.findUserInfoById(userId)
                               .ifPresent(userInfo -> {
                                 
                                 log.debug("{}action=Create Project addProjectInfoInUserInfo = {}", CREATE_PROJECT_PATH,
                                     userInfo.getProjects()
                                             .values());
                                 
                                 model.addAttribute("projectList", userInfo.getProjects()
                                                                           .values());
                               });
                  });
    
    model.addAttribute("createProjectParam", param);
    return "project-management";
  }
  
  // TODO UPDATE
  @PostMapping(path = UPDATE_PROJECT_PATH, params = "action=Save")
  public String toRenamePAGE(CreateProjectParam param, String projectId, Model model, Principal principal,
      String newProjectName) {
    log.debug("{}[Rename={}]", UPDATE_PROJECT_PATH, newProjectName);
    
    log.debug("CreateProjectParam id={}", projectId);
    model.addAttribute("action", "Save");
    model.addAttribute("createProjectParam", param);
    return "project-management";
  }
  
  public static String getDeleteProjectPath(String projectId) {
    return String.format("%s%s/%s", DELETE_PROJECT_PATH, ProjectController.PATH, projectId);
  }
  
  @GetMapping(DELETE_PROJECT_PATH + ProjectController.PATH + "/{projectId:.+}")
  public String deleteProject(@PathVariable String projectId, Principal principal, Model model) {
    log.debug("deleteProject={}", "getMapping");
    final String userId = principal.getName();
    log.info("[deleteProject] projectId={}, userId={}", projectId, userId);
    projectService.deleteProject(userId, projectId);
    userService.findUserInfoById(userId)
               .ifPresent(user -> {
                 model.addAttribute("projectList", user.getProjects()
                                                       .values());
               });
    
    CreateProjectParam defaultParam = CreateProjectParam.builder()
                                                        .projectName(DEFAULT_PROJECT_NAME)
                                                        .projectType(ProjectType.ABNORMAL_DETECTION)
                                                        .build();
    
    model.addAttribute("createProjectParam", defaultParam);
    return "project-management";
  }
  // TODO update project
  
}
