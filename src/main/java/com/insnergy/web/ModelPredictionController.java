package com.insnergy.web;

import java.security.Principal;
import java.util.List;
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
import com.insnergy.service.rest.ModelPredictionService;
import com.insnergy.service.rest.ModelPredictionService.GetModelSearchInput;
import com.insnergy.service.rest.ModelPredictionService.GetModelSearchOutput;
import com.insnergy.service.rest.ModelPredictionService.GetSearchModel;
import com.insnergy.service.rest.ModelPredictionService.PostClassificationModelPredictOutput;
import com.insnergy.service.rest.ModelPredictionService.PostModelPredictInput;
import com.insnergy.service.rest.ModelPredictionService.PostModelPredictOutput;
import com.insnergy.service.rest.ModelPredictionService.PostRegressionModelPredictOutput;
import com.insnergy.util.KnnPredictionUtil;
import com.insnergy.vo.CsvInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ModelPredictionController {
  
  private static final String PATH = "/model-prediction";
  private static final String REGRESSION = "/regression-model-prediction";
  private static final String CLASSIFICATION = "/classification-model-prediction";
  
  private final ModelPredictionService modelPredictionService;
  private final Map<String, List<GetSearchModel>> searchModelOutputListCache;
  private final UserInfoService userService;
  
  public ModelPredictionController(UserInfoService userService, ModelPredictionService modelPredictionService) {
    this.userService = userService;
    this.modelPredictionService = modelPredictionService;
    this.searchModelOutputListCache = new ConcurrentHashMap<>();
  }
  
  /**
   * 
   * Model Prediction Page<AbnormalDetection>
   */
  @GetMapping("/{projectId:.+}" + PATH + "/{fileId:.+}")
  public String toModelPredictionPage(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    log.debug("{}, projectId={}, fileId={}", PATH, projectId, fileId);
    
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), projectId, fileId);
    
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modePredictionMessage", "error fileId");
      return "redirect:/";
    }
    _csvInfo.ifPresent(csvInfo -> {
      List<String> traingColumn = csvInfo.getColumnNames()
                                             .subList(0, 2);
      log.debug("traingColumn={}", traingColumn);
      final GetModelSearchInput input = GetModelSearchInput.builder()
                                                           .server(csvInfo.getServer())
                                                           .build();
      log.debug("GetModelSearchInput input={}", input);
      
      Optional<GetModelSearchOutput> result = modelPredictionService.getModelSearch(input, fileId);
      log.debug("GetModelSearchOutput result={}", result);
      
      if (result.isPresent()) {
        GetModelSearchOutput output = result.get();
        List<GetSearchModel> modelList = output.getModel_list();
        log.debug("GetmodelList={}", modelList);
        
        searchModelOutputListCache.put(csvInfo.getFileId(), modelList);
        model.addAttribute("modelList", modelList);
        
        String modelId = "";
        if (modelList.size() > 0) {
          modelId = modelList.get(0)
                             .getModel_id();
        }
        
        String labelColumn = csvInfo.getColumnNames()
                                        .subList(2, 3)
                                        .get(0);
        
        DoModelPredictionParam defaultPredictInputParam = DoModelPredictionParam.builder()
                                                                                .fileId(csvInfo.getFileId())
                                                                                .projectId(csvInfo.getProjectId())
                                                                                .modelId(modelId)
                                                                                .labelColumn(labelColumn)
                                                                                .build();
        log.debug("defaultPredictInputParam={}", defaultPredictInputParam);
        model.addAttribute("predictInputParam", defaultPredictInputParam);
      }
      
      model.addAttribute("csvInfo", csvInfo);
      
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    
    return "model-prediction";
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoModelPredictionParam {
    String fileId;
    String modelId;
    String projectId;
    String labelColumn;
  }
  
  /**
   * 
   * Model Predict <AbnormalDetection>
   */
  @PostMapping(path = PATH, params = "action=Predict")
  public String doModelPredict(DoModelPredictionParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    log.debug("{}[action=Predict] param={}", PATH, param);
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), param.getProjectId(),
        param.getFileId());
    _csvInfo.ifPresent(csvInfo -> {
      String projectType = csvInfo.getProjectType();
      
      model.addAttribute("modelList", searchModelOutputListCache.get(csvInfo.getFileId()));
      
      final PostModelPredictInput predictInput = PostModelPredictInput.builder()
                                                                      .server(csvInfo.getServer())
                                                                      .modelId(param.getModelId())
                                                                      .fileId(param.getFileId())
                                                                      .projectType(projectType)
                                                                      .build();
      log.info("predictInput: {}", predictInput);
      Optional<PostClassificationModelPredictOutput> _output = modelPredictionService.postModelPredict(predictInput);
      
      if (_output.isPresent()) {
        log.info("ClassificationPredictResult: {}", _output);
        PostClassificationModelPredictOutput predictOutput = _output.get();
        if ((KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                       .getClassificationReport())) != null
            && (KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                          .getConfusionMatrix())) != null) {
          String[][] classificationReportArray = KnnPredictionUtil.classificationReportOutput(
              predictOutput.getPerformance()
                           .getClassificationReport());
          String[][] confusionMatrixArray = KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                                                      .getConfusionMatrix());
          Map<Integer, List<String>> reportMap = KnnPredictionUtil.doReportMapping(classificationReportArray);
          System.out.println("AbnormalClassificationReportArrayMappingEnd");
          reportMap.forEach((k, v) -> System.out.println("Report_Key:" + k + "Report_Value" + v));
          
          Map<Integer, List<String>> confusionMatrixMap = KnnPredictionUtil.doReportMapping(confusionMatrixArray);
          System.out.println("AbnormalConfusionMatrixMapmapEnd");
          confusionMatrixMap.forEach((k, v) -> System.out.println("Report_Key:" + k + "Report_Value" + v));
          
          model.addAttribute("reportMap", reportMap);
          model.addAttribute("confusionMatrixMap", confusionMatrixMap);
          
        }
        
        log.debug("AbnormalPredictOutputToThymleaf: {}", predictOutput);
        model.addAttribute("predictOutput", predictOutput);
      }
      model.addAttribute("csvInfo", csvInfo);
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    model.addAttribute("action", "Predict");
    model.addAttribute("predictInputParam", param);
    return "model-prediction";
  }
  
  
  @PostMapping(path = PATH, params = "action=Cancel")
  public String cancel(Model model, String projectId) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
  /**
   * 
   * Model Prediction Page<Regression>
   */
  @GetMapping("/{projectId:.+}" + REGRESSION + "/{fileId:.+}")
  public String toRegressionModelPredictionPage(@PathVariable String projectId, @PathVariable String fileId,
      Principal principal, Model model, RedirectAttributes redirectAttributes) {
    log.debug("{}, projectId={}, fileId={}", REGRESSION, projectId, fileId);
    
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), projectId, fileId);
    
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modePredictionMessage", "error fileId");
      return "redirect:/";
    }
    _csvInfo.ifPresent(csvInfo -> {
      List<String> traingColumn = csvInfo.getColumnNames()
                                             .subList(0, 2);
      log.debug("traingColumn={}", traingColumn);
      final GetModelSearchInput input = GetModelSearchInput.builder()
                                                           .server(csvInfo.getServer())
                                                           .build();
      log.debug("GetModelSearchInput input={}", input);
      
      Optional<GetModelSearchOutput> result = modelPredictionService.getModelSearch(input, fileId);
      log.debug("GetModelSearchOutput result={}", result);
      
      if (result.isPresent()) {
        GetModelSearchOutput output = result.get();
        List<GetSearchModel> modelList = output.getModel_list();
        log.debug("GetmodelList={}", modelList);
        
        searchModelOutputListCache.put(csvInfo.getFileId(), modelList);
        model.addAttribute("modelList", modelList);
        
        String modelId = "";
        if (modelList.size() > 0) {
          modelId = modelList.get(0)
                             .getModel_id();
        }
        
        String labelColumn = csvInfo.getColumnNames()
                                        .subList(0, 2)
                                        .get(0);
        
        log.debug("labelColumn_csvInfo.getColumnNames()subList(0, 2)get(0)={}", labelColumn);
        DoModelPredictionParam defaultPredictInputParam = DoModelPredictionParam.builder()
                                                                                .fileId(csvInfo.getFileId())
                                                                                .projectId(csvInfo.getProjectId())
                                                                                .modelId(modelId)
                                                                                .labelColumn(labelColumn)
                                                                                .build();
        log.debug("defaultPredictInputParam={}", defaultPredictInputParam);
        model.addAttribute("predictInputParam", defaultPredictInputParam);
      }
      
      model.addAttribute("csvInfo", csvInfo);
      
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    
    return "regression-model-prediction";
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoRegressionModelPredictionParam {
    String fileId;
    String modelId;
    String projectId;
    String labelColumn;
  }
  
  /**
   * 
   * Model Predict <Regression>
   */
  @PostMapping(path = REGRESSION, params = "action=Predict")
  public String doRegressionModelPredict(DoRegressionModelPredictionParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    log.debug("{}[action=RegressionPredict] param={}", REGRESSION, param);
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), param.getProjectId(),
        param.getFileId());
    _csvInfo.ifPresent(csvInfo -> {
      String projectType = csvInfo.getProjectType();
      
      model.addAttribute("modelList", searchModelOutputListCache.get(csvInfo.getFileId()));
      
      final PostModelPredictInput predictInput = PostModelPredictInput.builder()
                                                                      .server(csvInfo.getServer())
                                                                      .modelId(param.getModelId())
                                                                      .fileId(param.getFileId())
                                                                      .projectType(projectType)
                                                                      .build();
      log.info("predictInput: {}", predictInput);
      Optional<PostRegressionModelPredictOutput> _output = modelPredictionService.postRegressionModelPredict(
          predictInput);
      
      if (_output.isPresent()) {
        log.info("regressionPredictResult: {}", _output);
        PostRegressionModelPredictOutput predictOutput = _output.get();
        log.debug("regressionPredictOutputToThymleaf: {}", predictOutput);
        model.addAttribute("predictOutput", predictOutput);
      }
      model.addAttribute("csvInfo", csvInfo);
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    model.addAttribute("action", "Predict");
    model.addAttribute("predictInputParam", param);
    return "regression-model-prediction";
  }
  
  @PostMapping(path = REGRESSION, params = "action=Cancel")
  public String cancelForRegression(Model model, String projectId) {
    log.debug("{}[action=Cancel]", REGRESSION);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
  /**
   * 
   * Model Prediction Page<Classification>
   */
  // TODO
  @GetMapping("/{projectId:.+}" + CLASSIFICATION + "/{fileId:.+}")
  public String toClassificationPredictionPage(@PathVariable String projectId, @PathVariable String fileId,
      Principal principal, Model model, RedirectAttributes redirectAttributes) {
    log.debug("{}, projectId={}, fileId={}", CLASSIFICATION, projectId, fileId);
    
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), projectId, fileId);
    
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modePredictionMessage", "error fileId");
      return "redirect:/";
    }
    _csvInfo.ifPresent(csvInfo -> {
      List<String> traingColumn = csvInfo.getColumnNames()
                                             .subList(0, 2);
      log.debug("traingColumn={}", traingColumn);
      final GetModelSearchInput input = GetModelSearchInput.builder()
                                                           .server(csvInfo.getServer())
                                                           .build();
      log.debug("GetModelSearchInput input={}", input);
      
      Optional<GetModelSearchOutput> result = modelPredictionService.getModelSearch(input, fileId);
      log.debug("GetModelSearchOutput result={}", result);
      
      if (result.isPresent()) {
        GetModelSearchOutput output = result.get();
        List<GetSearchModel> modelList = output.getModel_list();
        log.debug("GetmodelList={}", modelList);
        
        searchModelOutputListCache.put(csvInfo.getFileId(), modelList);
        model.addAttribute("modelList", modelList);
        
        String modelId = "";
        if (modelList.size() > 0) {
          modelId = modelList.get(0)
                             .getModel_id();
        }
        
        String labelColumn = csvInfo.getColumnNames()
                                        .subList(2, 3)
                                        .get(0);
        
        DoModelPredictionParam defaultPredictInputParam = DoModelPredictionParam.builder()
                                                                                .fileId(csvInfo.getFileId())
                                                                                .projectId(csvInfo.getProjectId())
                                                                                .modelId(modelId)
                                                                                .labelColumn(labelColumn)
                                                                                .build();
        log.debug("defaultPredictInputParam={}", defaultPredictInputParam);
        model.addAttribute("predictInputParam", defaultPredictInputParam);
      }
      
      model.addAttribute("csvInfo", csvInfo);
      
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    
    return "classification-model-prediction";
    
  }
  
  /**
   * 
   * Model Predict<Classification>
   */
  // TODO
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoKnnModelPredictionParam {
    String fileId;
    String modelId;
    String projectId;
    String labelColumn;
  }
  
  @PostMapping(path = CLASSIFICATION, params = "action=Predict")
  public String doClassificationModelPredict(DoKnnModelPredictionParam param, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    log.debug("{}[action=RegressionPredict] param={}", PATH, param);
    final Optional<CsvInfo> _csvInfo = userService.getUserProjectCsv(principal.getName(), param.getProjectId(),
        param.getFileId());
    _csvInfo.ifPresent(csvInfo -> {
      String projectType = csvInfo.getProjectType();
      
      model.addAttribute("modelList", searchModelOutputListCache.get(csvInfo.getFileId()));
      
      final PostModelPredictInput predictInput = PostModelPredictInput.builder()
                                                                      .server(csvInfo.getServer())
                                                                      .modelId(param.getModelId())
                                                                      .fileId(param.getFileId())
                                                                      .projectType(projectType)
                                                                      .build();
      log.info("predictInput: {}", predictInput);
      Optional<PostClassificationModelPredictOutput> _output = modelPredictionService.postClassificationModelPredict(
          predictInput);
      if (_output.isPresent()) {
        log.info("ClassificationPredictResult: {}", _output);
        PostClassificationModelPredictOutput predictOutput = _output.get();
        if ((KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                       .getClassificationReport())) != null
            && (KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                          .getConfusionMatrix())) != null) {
          String[][] classificationReportArray = KnnPredictionUtil.classificationReportOutput(
              predictOutput.getPerformance()
                           .getClassificationReport());
          String[][] confusionMatrixArray = KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                                                      .getConfusionMatrix());
          Map<Integer, List<String>> reportMap = KnnPredictionUtil.doReportMapping(classificationReportArray);
          System.out.println("ClassificationReportArrayMappingEnd");
          reportMap.forEach((k, v) -> System.out.println("Report_Key:" + k + "Report_Value" + v));
          
          Map<Integer, List<String>> confusionMatrixMap = KnnPredictionUtil.doReportMapping(confusionMatrixArray);
          System.out.println("ConfusionMatrixMapmapEnd");
          confusionMatrixMap.forEach((k, v) -> System.out.println("Report_Key:" + k + "Report_Value" + v));
          
          model.addAttribute("reportMap", reportMap);
          model.addAttribute("confusionMatrixMap", confusionMatrixMap);
          
        }
        
        log.debug("ClassificationPredictOutputToThymleaf: {}", predictOutput);
        model.addAttribute("predictOutput", predictOutput);
      }
      model.addAttribute("csvInfo", csvInfo);
    });
    
    model.addAttribute("sidebarActiveId", "sidebar-model-prediction");
    model.addAttribute("action", "Predict");
    model.addAttribute("predictInputParam", param);
    // TODO
    return "classification-model-prediction";
  }
  
  @PostMapping(path = CLASSIFICATION, params = "action=Cancel")
  public String cancelForClassification(Model model, String projectId) {
    log.debug("{}[action=Cancel]", CLASSIFICATION);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
}
