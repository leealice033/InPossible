package com.insnergy.web;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.insnergy.domain.ModelEntity;
import com.insnergy.repo.ModelEntityRepo;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ModelService;
import com.insnergy.service.rest.UserModelService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ModelManagementController {
  
  static final String PATH = "/model";
  public static final String MODEL_PATH = "/model-management";
  public static final String DELETE_MODEL_PATH = "/model-delete";
  private final UserInfoService userService;
  private final ModelService modelService;
  private final UserModelService userModelService;
  private final ModelEntityRepo modelRepo;
  
  public ModelManagementController(UserInfoService userService, ModelService modelService,
      UserModelService userModelService, ModelEntityRepo modelRepo) {
    this.userService = userService;
    this.modelService = modelService;
    this.userModelService = userModelService;
    this.modelRepo = modelRepo;
  }
  
  @GetMapping("/refresh/{projectId:.+}")
  public String refreshModelInfo(@PathVariable String projectId, Model model, Principal principal) {
    log.debug("refreshModelInfo");
    
    userService.refreshUserProject(principal.getName(), projectId);
    
    return "redirect:" + MODEL_PATH + "/" + projectId;
  }
  
  // FIXME
  @GetMapping("/project/{projectId:.+}" + MODEL_PATH)
  public String toModelPage(@PathVariable String projectId, Model model, Principal principal) {
    final String userId = principal.getName();
    log.debug("index userId={}", userId);
    
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      final ProjectInfo project = userInfo.getProjects()
                                          .get(projectId);
      
      if (project != null && project.getModels() != null && project.getModels()
                                                                   .size() == 0) {
        userModelService.getUserProjectModel(AnalysisServer.PYTHON, userId, projectId)
                        .ifPresent(output -> {
                          if (project != null) {
                            project.setModels(output.getModelList());
                          }
                        });
      }
      
      model.addAttribute("project", project);
      
    });
    return "model-management";
  }
  
  public static String getDeleteModelPath(String projectId, String modelId) {
    log.debug("deleteMode={}",
        String.format("%s/%s%s/%s", ProjectController.PATH, projectId, DELETE_MODEL_PATH, modelId));
    return String.format("%s/%s%s/%s", ProjectController.PATH, projectId, DELETE_MODEL_PATH, modelId);
  }
  
  @GetMapping("/project/{projectId:.+}" + DELETE_MODEL_PATH + "/{modelId:.+}")
  public String deleteModel(@PathVariable String projectId, @PathVariable String modelId, Principal principal) {
    log.info("[deleteModel] projectId={}, modelId={}", projectId, modelId);
    final String userId = principal.getName();
    
    deleteModelById(modelId, projectId, userId);
    
    return "redirect:" + ProjectController.PATH + "/" + projectId + MODEL_PATH;
  }
  
  public void deleteModelById(String modelId, final String projectId, final String userId) {
    log.debug("enter_deleteModelById_modelId={},userId={}, projectId={}", modelId, userId, projectId);
    if (userService.getUserProjectModel(userId, projectId, modelId)
                   .isPresent()) {
      // if target model is exist
      log.debug("target model to delete is ={}", userService.getUserProjectModel(userId, projectId, modelId)
                                                            .get());
      
      Optional<ProjectInfo> _project = userService.getUserProject(userId, projectId);
      _project.ifPresent(projectInfo -> {
        log.debug("enter_ddeleteModelById_modelId={},projectInfo={}", modelId, _project);
        if (deleteModelPython(modelId)) {
          log.debug("PYTHON model deleted_id={}", modelId);
          if (deleteModelDB(modelId)) {
            log.debug("DB model deleted_id={}", modelId);
            projectInfo.getModels()
                       .remove(userService.getUserProjectModel(userId, projectId, modelId)
                                          .get());
          }
        }
      });
    } else {
      log.debug("target model doesn't exist!,id={}", modelId);
    }
    
  }
  
  private Boolean deleteModelPython(String modelId) {
    Boolean result = false;
    if (modelService.deleteModel(AnalysisServer.PYTHON, modelId)
                    .isPresent()) {
      result = true;
      log.debug("deleteModelPython={}", result);
    }
    return result;
    
  }
  
  private Boolean deleteModelDB(String modelId) {
    Boolean result = false;
    if (modelRepo.findOneById(modelId)
                 .isPresent()) {
      ModelEntity modelEntity = modelRepo.findOneById(modelId)
                                         .get();
      log.debug("findModelEntityById{}={}", modelId, modelEntity);
      modelRepo.delete(modelEntity);
      result = true;
    } else {
      log.debug("can't find model entity by Id={}", modelId);
    }
    return result;
  }
  
  @PostMapping(path = MODEL_PATH, params = "action=Back")
  public String cancelRegression(Model model, String projectId) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
}
