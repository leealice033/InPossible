package com.insnergy.web;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ModelService;
import com.insnergy.service.rest.UserModelService;
import com.insnergy.service.rest.ModelService.PutModelInput;
import com.insnergy.service.rest.ModelService.PutModelOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//TODO
@Controller
@Slf4j
public class ModelUpdateController {
  private static final String MODEL_PATH = "/model-management";
  private static final String EDIT_MODEL = "/update-model";
  
  private final UserInfoService userService;
  private final UserModelService userModelService;// for refresh
  private final ModelService modelService;
  private final Map<String, DoUpdateModelParam> editModelInputCache;
  
  public ModelUpdateController(UserInfoService userService, UserModelService userModelService,
      ModelService modelService) {
    this.userService = userService;
    this.userModelService = userModelService;
    this.modelService = modelService;
    this.editModelInputCache = new ConcurrentHashMap<>();
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoUpdateModelParam {
    String modelId;
    String modelName;
    String projectId;
  }
  
  // TODO
  public String refreshModelPage(String projectId, Principal principal, Model model) {
    final String userId = principal.getName();
    
    userService.refreshUserProject(userId, projectId);
    
    return "redirect:" + "project/" + projectId + '/' + MODEL_PATH;
  }
  
  // TODO
  @GetMapping(EDIT_MODEL + "/{modelId:.+}")
  public String toUpdateModelPage(@PathVariable String modelId, String projectId, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    // String userId = principal.getName();
    modelService.getModel(AnalysisServer.PYTHON, modelId)
                .ifPresent(getModel -> {
                  final DoUpdateModelParam builder = DoUpdateModelParam.builder()
                                                                       .modelId(modelId)
                                                                       .modelName(getModel.getModelList()
                                                                                          .get(0)
                                                                                          .getModelName())
                                                                       .projectId(projectId)
                                                                       .build();
                  
                  log.debug("DoUpdateModelParam_builder={}", builder);
                  model.addAttribute("updateModelParam", builder);
                });
    
    model.addAttribute("action", " ");
    return "update-model-page";
  }
  
  // TODO cancel
  @PostMapping(path = EDIT_MODEL, params = "action=Cancel")
  public String cancel(String projectId, Model model) {
    log.debug("{}[action=Cancel]", EDIT_MODEL);
    return "redirect:" + "project/" + projectId + '/' + MODEL_PATH;
    
  }
  
  // TODO saveChange
  @PostMapping(path = EDIT_MODEL, params = "action=SaveChange")
  public String saveModelAfterEdit(DoUpdateModelParam param, Model model, Principal principal) {
    String userId = principal.getName();
    log.debug("{}[action=Save(EDIT_MODEL)] param={}", EDIT_MODEL, param);
    
    final PutModelInput editParams = PutModelInput.builder()
                                                  .modelName(param.getModelName())
                                                  .build();
    
    final DoUpdateModelParam _temp = DoUpdateModelParam.builder()
                                                       .modelId(param.getModelId())
                                                       .modelName(param.getModelName())
                                                       .projectId(param.getProjectId())
                                                       .build();
    
    log.debug("{}[action=SaveAfterEdit(model)] PutModelInput={}", EDIT_MODEL, editParams);
    
    editModelInputCache.put(param.getModelName(), _temp);
    log.debug("{}[action=Save(EDIT_MODEL)] editModelInputCache={}", EDIT_MODEL, editModelInputCache);
    log.debug("{}[action=Save(EDIT_MODEL)] makeModel_temp={}", EDIT_MODEL, _temp);
    
    model.addAttribute("updateModelParam", _temp);
    model.addAttribute("userId", userId);
    
    // TODO call python Api
    Optional<PutModelOutput> _res = modelService.updateModel(AnalysisServer.PYTHON, param.getModelId(), editParams);
    log.debug("PutModelOutput_res.isPresent={}", _res);
    
    if (_res.isPresent()) {
      PutModelOutput res = _res.get();
      log.debug("_res.isPresent={}", res);
      if ("ok".equalsIgnoreCase(res.getStatus())) {
        model.addAttribute("action", "SaveChange");
        
        log.debug("action=PutModelOutput Result={}", res);
        // rename updateModel in modelInfo
        userService.getUserProjectModel(userId, res.getProjectId(), res.getModelId())
                   .ifPresent(updateModel -> {
                     updateModel.setModelName(res.getModelName());
                   });
      }
      
    }
    return refreshModelPage(param.getProjectId(), principal, model);
  }
}
