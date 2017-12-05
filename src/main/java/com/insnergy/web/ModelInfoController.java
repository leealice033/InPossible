package com.insnergy.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ModelTrainingService;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewInput;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewOutput;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ModelInfo.ModelInfoAction;
import com.insnergy.web.algo.ModelTrainingPageController.PreviewParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j

public class ModelInfoController {
  private static final String MODEL_INFO = "/model-info";
  private static final String ABNORMAL_DETECTION = "/abnormal-detection-model-info";
  private static final String REGRESSION = "/regression-model-info";
  private static final String CLASSIFICATION = "/classification-model-info";
  private static final String CLUSTERING = "/clustering-model-info";
  private final UserInfoService userInfoService;
  private final ModelTrainingService modelTrainingservice;
  
  public ModelInfoController(UserInfoService userInfoService, ModelTrainingService modelTrainingservice) {
    this.userInfoService = userInfoService;
    this.modelTrainingservice = modelTrainingservice;
  }
  
  @GetMapping("/project/{projectId:.+}" + MODEL_INFO + "/{modelId:.+}")
  public String toModelInfoPage(@PathVariable String projectId, @PathVariable String modelId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    if (userInfoService.getUserProjectModel(userId, projectId, modelId)
                       .isPresent()) {
      ModelInfo modelInfo = userInfoService.getUserProjectModel(userId, projectId, modelId)
                                           .get();
      log.debug("model_info_in_model={}", modelInfo);
      
      if (userInfoService.getUserProjectModel(userId, projectId, modelId)
                         .get()
                         .getActions()
                         .get(0)
                         .getFunction()
                         .equals("source")) {
        ModelInfoAction modelAction = userInfoService.getUserProjectModel(userId, projectId, modelId)
                                                     .get()
                                                     .getActions()
                                                     .get(0);
        String fileId = modelAction.getValue();
        log.debug("modelActionSourceCsvId={}", fileId);
        
        if (fileId != null) {
          log.debug("modelCsvSource_fileId={}", fileId);
          CsvInfo csvInfo = userInfoService.getUserProjectCsv(userId, projectId, fileId)
                                           .get();
          model.addAttribute("csvInfo", csvInfo);
          // FIXME
          /**
           * ---<Classification> PreviewParam
           * ----的xy坐標設定--------------------------------------
           * xy可以是除了label之外的所有csv檔案的欄位名稱(columnName)
           */
          if (StringUtils.equals("classification", modelInfo.getProjectType())) {
            final com.insnergy.web.algo.ModelTrainingPageController.PreviewParam previewParam;
            List<String> columnNamesCanChoose = new ArrayList<>();
            for (String columnName : csvInfo.getColumnNames()) {
              if (!columnName.equals(csvInfo.getLabel()))
                columnNamesCanChoose.add(columnName);
            }
            
            for (String choose : columnNamesCanChoose) {
              log.debug("columnNamesCanChoose={}", choose);
            }
            if (columnNamesCanChoose.size() >= 2) {
              previewParam = PreviewParam.builder()
                                         .x(columnNamesCanChoose.get(0))
                                         .y(columnNamesCanChoose.get(1))
                                         .build();
            } else {
              log.error("PreviewParam error: {}", "ColumnName size < 2");
              previewParam = PreviewParam.builder()
                                         .build();
            }
            
            model.addAttribute("columnNamesCanChoose", columnNamesCanChoose);
            model.addAttribute("previewParam", previewParam);
          } /**
             * --除了<Classification>
             * 以外的的xy坐標設定--------------------------------------
             */
          else {
            final PreviewParam previewParam;
            if (csvInfo.getColumnNames()
                       .size() >= 2) {
              previewParam = PreviewParam.builder()
                                         .x(csvInfo.getColumnNames()
                                                   .get(0))
                                         .y(csvInfo.getColumnNames()
                                                   .get(1))
                                         .build();
            } else {
              log.error("PreviewParam error: {}", "ColumnName size < 2");
              previewParam = PreviewParam.builder()
                                         .build();
            }
            model.addAttribute("previewParam", previewParam);
          } 
        } else {
          log.error("FileId not found");
        }
        model.addAttribute("modelInfo", modelInfo);
      }
      
    } else {
      log.debug("model not found Id={}", modelId);
    } 
    return "model-info";
  }
  
  /**
   * 
   * Model Preview <AbnormalDetection>
   */
  @PostMapping(path = ABNORMAL_DETECTION, params = "action=View")
  public String preview(PreviewParam param, Principal principal, Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    ModelInfo modelInfo = getModelByParam(param, userId);
    log.debug("model_info_in_model={}", modelInfo);
    model.addAttribute("modelInfo", modelInfo);
    
    final Optional<CsvInfo> _csvFileInfo = userInfoService.getUserProjectCsv(userId, param.getProjectId(),
        param.getFileId());
    if (!_csvFileInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    }
    
    _csvFileInfo.ifPresent(csvFileInfo -> {
      model.addAttribute("csvInfo", csvFileInfo);
      
      final PreviewParam previewParam = PreviewParam.builder()
                                                    .x(param.getX())
                                                    .y(param.getY())
                                                    .build();
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("projectType", csvFileInfo.getProjectType());
      
      final PostModelPreviewInput input = PostModelPreviewInput.builder()
                                                               .server(csvFileInfo.getServer())
                                                               .fileId(csvFileInfo.getFileId())
                                                               .modelId(param.getModelId())
                                                               .xAxis(param.getX())
                                                               .yAxis(param.getY())
                                                               .build();
      
      Optional<PostModelPreviewOutput> _res = modelTrainingservice.postModelPreview(input);
      _res.ifPresent(res -> {
        log.debug("trainingModelPreview_imgUrl={}", res.getImageUrl());
        
        model.addAttribute("imageUrl", res.getImageUrl());
        
      });
      
    });
    
    model.addAttribute("action", "Preview");
    model.addAttribute("sidebarActiveId", "sidebar-model-training");
    
    return "model-info";
  }
  
  /**
   * 
   * Model Preview <Regression>
   */
  @PostMapping(path = REGRESSION, params = "action=View")
  public String regressionModelPreview(PreviewParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    ModelInfo modelInfo = getModelByParam(param, userId);
    log.debug("model_info_in_model={}", modelInfo);
    model.addAttribute("modelInfo", modelInfo);
    
    final Optional<CsvInfo> _csvFileInfo = userInfoService.getUserProjectCsv(userId, param.getProjectId(),
        param.getFileId());
    if (!_csvFileInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    }
    
    _csvFileInfo.ifPresent(csvFileInfo -> {
      model.addAttribute("csvInfo", csvFileInfo);
      
      final PreviewParam previewParam = PreviewParam.builder()
                                                    .x(param.getX())
                                                    .y(param.getY())
                                                    .build();
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("projectType", csvFileInfo.getProjectType());// projectType
      log.debug("REGRESSION_projectTypeCheck={}", csvFileInfo.getProjectType());
      
      final PostModelPreviewInput input = PostModelPreviewInput.builder()
                                                               .server(csvFileInfo.getServer())
                                                               .fileId(csvFileInfo.getFileId())
                                                               .modelId(param.getModelId())
                                                               .xAxis(param.getX())
                                                               .yAxis(param.getY())
                                                               .build();
      
      Optional<PostModelPreviewOutput> _res = modelTrainingservice.postModelPreview(input);
      _res.ifPresent(res -> {
        log.debug("trainingModelPreview_imgUrl={}", res.getImageUrl());
        
        model.addAttribute("imageUrl", res.getImageUrl());
        
      });
      
    });
    model.addAttribute("action", "Preview");
    model.addAttribute("sidebarActiveId", "sidebar-model-training");
    
    return "model-info";
  }
  
  /**
   * 
   * Model Preview <Classification>
   */
  @PostMapping(path = CLASSIFICATION, params = "action=View")
  public String classificationModelPreview(PreviewParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    ModelInfo modelInfo = getModelByParam(param, userId);
    log.debug("model_info_in_model={}", modelInfo);
    model.addAttribute("modelInfo", modelInfo);
    
    final Optional<CsvInfo> _csvFileInfo = userInfoService.getUserProjectCsv(userId, param.getProjectId(),
        param.getFileId());
    if (!_csvFileInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    }
    
    _csvFileInfo.ifPresent(csvFileInfo -> {
      model.addAttribute("csvInfo", csvFileInfo);
      
      final PreviewParam previewParam = PreviewParam.builder()
                                                    .x(param.getX())
                                                    .y(param.getY())
                                                    .build();
      
      List<String> columnNamesCanChoose = new ArrayList<>();
      for (String columnName : csvFileInfo.getColumnNames()) {
        if (!columnName.equals(csvFileInfo.getLabel()))
          columnNamesCanChoose.add(columnName);
        
      }
      
      for (String choose : columnNamesCanChoose) {
        log.debug("columnNamesCanChoose={}", choose);
      }
      
      model.addAttribute("columnNamesCanChoose", columnNamesCanChoose);
      
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("projectType", csvFileInfo.getProjectType());
      log.debug("CLASSIFICATION_projectTypeCheck={}", csvFileInfo.getProjectType());
      
      final PostModelPreviewInput input = PostModelPreviewInput.builder()
                                                               .server(csvFileInfo.getServer())
                                                               .fileId(csvFileInfo.getFileId())
                                                               .modelId(param.getModelId())
                                                               .xAxis(param.getX())
                                                               .yAxis(param.getY())
                                                               .build();
      
      Optional<PostModelPreviewOutput> _res = modelTrainingservice.postModelPreview(input);
      _res.ifPresent(res -> {
        log.debug("trainingModelPreview_imgUrl={}", res.getImageUrl());
        
        model.addAttribute("imageUrl", res.getImageUrl());
        
      });
      
    });
    model.addAttribute("action", "Preview");
    model.addAttribute("sidebarActiveId", "sidebar-model-training");
    
    return "model-info";
  }
  
  /**
   * 
   * Model Preview <Clustering>
   */
  @PostMapping(path = CLUSTERING, params = "action=View")
  public String clusteringModelPreview(PreviewParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    ModelInfo modelInfo = getModelByParam(param, userId);
    log.debug("model_info_in_model={}", modelInfo);
    model.addAttribute("modelInfo", modelInfo);
    final Optional<CsvInfo> _csvFileInfo = userInfoService.getUserProjectCsv(userId, param.getProjectId(),
        param.getFileId());
    if (!_csvFileInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    }
    
    _csvFileInfo.ifPresent(csvFileInfo -> {
      model.addAttribute("csvInfo", csvFileInfo);
      
      final PreviewParam previewParam = PreviewParam.builder()
                                                    .x(param.getX())
                                                    .y(param.getY())
                                                    .build();
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("projectType", csvFileInfo.getProjectType());
      log.debug("Clustering_projectTypeCheck={}", csvFileInfo.getProjectType());
      
      final PostModelPreviewInput input = PostModelPreviewInput.builder()
                                                               .server(csvFileInfo.getServer())
                                                               .fileId(csvFileInfo.getFileId())
                                                               .modelId(param.getModelId())
                                                               .xAxis(param.getX())
                                                               .yAxis(param.getY())
                                                               .build();
      
      Optional<PostModelPreviewOutput> _res = modelTrainingservice.postModelPreview(input);
      _res.ifPresent(res -> {
        log.debug("trainingModelPreview_imgUrl={}", res.getImageUrl());
        
        model.addAttribute("imageUrl", res.getImageUrl());
        
      });
      
    });
    model.addAttribute("action", "Preview");
    model.addAttribute("sidebarActiveId", "sidebar-model-training");
    
    return "model-info";
  }
  
  private ModelInfo getModelByParam(PreviewParam param, String userId) {
    ModelInfo result = ModelInfo.builder()
                                .build();
    
    if (userInfoService.getUserProjectModel(userId, param.getProjectId(), param.getModelId())
                       .isPresent()) {
      result = userInfoService.getUserProjectModel(userId, param.getProjectId(), param.getModelId())
                              .get();
    }
    
    return result;
    
  }
}
