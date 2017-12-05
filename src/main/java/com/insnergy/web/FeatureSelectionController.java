package com.insnergy.web;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.CsvInfoService;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.FeatureSelectionService;
import com.insnergy.service.rest.FeatureSelectionService.PostFeatureSelectInput;
import com.insnergy.service.rest.FeatureSelectionService.PostFeatureSelectOutput;
import com.insnergy.service.rest.FeatureSelectionService.PostFeatureWeightInput;
import com.insnergy.service.rest.FeatureSelectionService.PostFeatureWeightOutput;
import com.insnergy.util.FeatureSelectionUtil;
import com.insnergy.util.Stages;
import com.insnergy.vo.CsvInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class FeatureSelectionController {
  
  private static final String PATH = "/feature-selection";
  private final UserInfoService userService;
  private final FeatureSelectionService featureSelectionService;
  private final CsvInfoService csvInfoService;
  
  public FeatureSelectionController(UserInfoService userService, FeatureSelectionService featureSelectionService,
      CsvInfoService csvInfoService) {
    this.userService = userService;
    this.featureSelectionService = featureSelectionService;
    this.csvInfoService = csvInfoService;
  }
  
  @ModelAttribute("sidebarActiveId")
  public String populateSidebarActiveId() {
    return "sidebar-feature-selection";
  }
  
  @GetMapping("/{projectId:.+}" + PATH + "/{fileId:.+}")
  public String toFeatureSelectionPage(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    log.debug("{} fileId={}", PATH, fileId);
    String userId = principal.getName();
    if (fileId == null) {
      redirectAttributes.addFlashAttribute("featureSelectionMessage", "error: selectFile is null");
      return "redirect:/";
    } else {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   model.addAttribute("csvInfo", csvInfo);
                 });
      
    }
    return "feature-selection";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class FeatureWeightRow {
    String featureName;
  }
  
  @PostMapping(path = PATH, params = "action=Apply Algorithm")
  public String computeFeatureWeight(String projectId, String fileId, Principal principal, String algorithmName,
      Model model, RedirectAttributes redirectAttributes) {
    log.debug("{}[action=Apply Algorithm] fileId={}, algorithmName={}", PATH, fileId, algorithmName);
    String userId = principal.getName();
    try {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   PostFeatureWeightInput featureWeightInput = PostFeatureWeightInput.builder()
                                                                                     .fileId(fileId)
                                                                                     .method(algorithmName)
                                                                                     .build();
                   Optional<PostFeatureWeightOutput> _res = featureSelectionService.postFeatureWeight(
                       csvInfo.getServer(), featureWeightInput);
                   
                   if (_res.isPresent()) {
                     PostFeatureWeightOutput res = _res.get();
                     log.debug("{}[action=Apply PostFeatureWeightResponse ={},", PATH, res);
                     if ("ok".equalsIgnoreCase(res.getStatus())) {
                       model.addAttribute("featureList", res.getFeatureList());
                       // FIXME
                       FeatureSelectionUtil.doubleValueSetDegits(res.getFeatureAttribute());
                       //
                       log.debug("{}[After_FeatureSelectionUtil.doubleValueSetDegits ={},", PATH,
                           res.getFeatureAttribute());
                       model.addAttribute("featureAttributeList", res.getFeatureAttribute());
                       
                     }
                   }
                   
                   model.addAttribute("csvInfo", csvInfo);
                 });
      
    } catch (Exception e) {
      final String message = ExceptionUtils.getMessage(e);
      log.error("postCsv error: {}", message, e);
      redirectAttributes.addFlashAttribute("featureSelectionMessage",
          String.format("compute weight fail: %s", message));
    }
    return "feature-selection";
  }
  
  @PostMapping(path = PATH, params = "action=Cancel")
  public String cancel(String projectId, Model model) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
  @PostMapping(path = PATH, params = "action=Save")
  public String save(String projectId, String fileId, Principal principal, String newFileName, String featureName,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    
    log.debug("{}[action=Save] fileId={}, newFileName={}, featureName={}", PATH, fileId, newFileName, featureName);
    
    if (StringUtils.isBlank(featureName)) {
      return toFeatureSelectionPage(projectId, fileId, principal, model, redirectAttributes);
    }
    
    try {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   List<String> featureList = Arrays.asList(featureName.split(","));
                   PostFeatureSelectInput featureSelectInput = PostFeatureSelectInput.builder()
                                                                                     .fileId(fileId)
                                                                                     .newFileName(newFileName)
                                                                                     .featureList(featureList)
                                                                                     .stage(Stages.MODEL_TRAINING)
                                                                                     .build();
                   
                   Optional<PostFeatureSelectOutput> _res = featureSelectionService.postFeatureSelect(
                       csvInfo.getServer(), featureSelectInput);
                   if (_res.isPresent()) {
                     
                     // csvInfo -> call python get csv by fileId
                     storeDBandResetInfo(userId, projectId, _res.get()
                                                                .getNewFileId());
                     
                     log.debug("{}[feature_selection_getUserProjectCsvList]  _res ={}", PATH, _res);
                     PostFeatureSelectOutput res = _res.get();
                     log.debug("{}feature_selection_PostFeatureSelectOutput_res ={}", PATH, res);
                     if ("ok".equalsIgnoreCase(res.getStatus())) {
                       userService.refreshUserProject(userId, projectId);
                     } else {
                       redirectAttributes.addFlashAttribute("featureSelectionMessage",
                           String.format("Feature Selection fail: %s", csvInfo.getFileName()));
                     }
                   }
                 });
      
    } catch (Exception e) {
      final String message = ExceptionUtils.getMessage(e);
      log.error("postCsv error: {}", message, e);
      redirectAttributes.addFlashAttribute("featureSelectionMessage",
          String.format("Feature Selection fail: %s", message));
    }
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
  private void storeDBandResetInfo(String userId, String projectId, String fileId) {
    // csvInfo -> call python get csv by fileId
    String resFileId = fileId;
    if (csvInfoService.buildCsvInfoStoreToDB(resFileId))
    // csvEntity -> build & csvInfo ->set index
    {
      log.debug("true");
      log.debug("after build db and set index_csvList={}", userService.getUserProjectCsvList(userId, projectId));
      if (userService.getUserProjectCsv(userId, projectId, resFileId)
                     .isPresent()) {
        CsvInfo checkCsv = userService.getUserProjectCsv(userId, projectId, resFileId)
                                      .get();
        if (checkCsv.getIndex() == null) {
          checkCsv.setIndex(csvInfoService.getDbIndex(resFileId));
          log.debug("after re chaeck index_csvList={}", userService.getUserProjectCsvList(userId, projectId));
        }
      }
    }
    
  }
  
}
