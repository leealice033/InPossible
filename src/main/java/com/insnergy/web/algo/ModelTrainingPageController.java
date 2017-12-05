package com.insnergy.web.algo;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.ModelInfoService;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.AlgorithmService;
import com.insnergy.service.rest.ModelPredictionService;
import com.insnergy.service.rest.ModelTrainingService;
import com.insnergy.service.rest.AlgorithmService.AlgoParameterOutput;
import com.insnergy.service.rest.AlgorithmService.PostDoAlgoInput;
import com.insnergy.service.rest.AlgorithmService.PostDoAlgoOutput;
import com.insnergy.service.rest.ModelPredictionService.PostClassificationModelPredictOutput;
import com.insnergy.service.rest.ModelPredictionService.PostModelPredictInput;
import com.insnergy.service.rest.ModelPredictionService.PostRegressionModelPredictOutput;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewInput;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.KnnPredictionUtil;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ModelInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ModelTrainingPageController {
  private static final String PATH = "/algo";
  private static final String PROJECT = "/project";
  private final UserInfoService userInfoService;
  private final ModelInfoService modelInfoService;
  private final AlgorithmService algorithmService;
  private final ModelTrainingService modelTrainingService;
  private final ModelPredictionService modelPredictionService;
  private final Map<String, PostDoAlgoInput> trainingInputCache;
  
  public ModelTrainingPageController(UserInfoService userInfoService, ModelInfoService modelInfoService,
      AlgorithmService algorithmService, ModelTrainingService modelTrainingService,
      ModelPredictionService modelPredictionService) {
    this.userInfoService = userInfoService;
    this.modelInfoService = modelInfoService;
    this.algorithmService = algorithmService;
    this.modelTrainingService = modelTrainingService;
    this.modelPredictionService = modelPredictionService;
    this.trainingInputCache = new ConcurrentHashMap<>();
    
  }
  
  /**
   * 
   * Model Training Page--Step 1 Show Algorithm
   */
  @GetMapping("/{projectId:.+}" + PATH + "/{fileId:.+}")
  public String toShowAlgoPage(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}, projectId={}, fileId={}", PATH, projectId, fileId);
    
    final Optional<CsvInfo> _csvInfo = userInfoService.getUserProjectCsv(userId, projectId, fileId);
    
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    } else {
      CsvInfo csvInfo = _csvInfo.get();
      model.addAttribute("csvInfo", csvInfo);
      algorithmService.getAlgoByProjectType(csvInfo.getServer(), csvInfo.getProjectType())
                      .ifPresent(output -> {
                        List<String> algoSelect = output.getAlgoList();
                        log.debug("algoSelect", algoSelect);
                        model.addAttribute("algoSelect", algoSelect);
                      });
      
    }
    
    return "algorithms/algo-model-training";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ParamForThymeleaf {
    String name;
    String type;
    List<String> range;
    String format;// boostrap
    String value;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class AlgoInputFormat {
    String projectType;
    String fileId;
    String modelMethod;
    String modelName;
    Map<String, ParamForThymeleaf> typeMap;
    Map<String, String> paramValueMap;
  }
  
  /**
   * 
   * Model Training Page--Step 2 Show parameters
   */
  
  // /// algo/{algo_name}/def
  @PostMapping(path = PATH, params = "action=Apply")
  public String toShowParameters(String projectId, String fileId, String modelMethod, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}_toShowParameters, projectId={}, fileId={}, methodName={}", PATH, projectId, fileId, modelMethod);
    final Optional<CsvInfo> _csvInfo = userInfoService.getUserProjectCsv(userId, projectId, fileId);
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    } else {
      model.addAttribute("action", "Apply");
      CsvInfo csvInfo = _csvInfo.get();
      model.addAttribute("csvInfo", csvInfo);
      
      algorithmService.getAlgoParameters(csvInfo.getServer(), modelMethod)
                      .ifPresent(output -> {
                        List<ParamForThymeleaf> paramToShow = buildParamForThymeleaf(output.getArgumentsDef());
                        log.debug("paramToShow={}", paramToShow);
                        model.addAttribute("parameters", paramToShow);
                        Map<String, String> defaultArgumentMap = new LinkedHashMap<>();
                        for (AlgoParameterOutput parameter : output.getArgumentsDef()) {
                          defaultArgumentMap.put(parameter.getName(), parameter.getDefaultValue());
                          log.debug("parameterToMap={}", defaultArgumentMap);
                        }
                        
                        Map<String, ParamForThymeleaf> _typeMap = new LinkedHashMap<>();
                        for (ParamForThymeleaf paramForThyme : paramToShow) {
                          _typeMap.put(paramForThyme.getName(), paramForThyme);
                        }
                        
                        AlgoInputFormat defaultMap = AlgoInputFormat.builder()
                                                                    .projectType(csvInfo.getProjectType())
                                                                    .fileId(csvInfo.getFileId())
                                                                    .modelMethod(modelMethod)
                                                                    .modelName(
                                                                        "new" + "_" + modelMethod + "_" + "model")
                                                                    .typeMap(_typeMap)
                                                                    .paramValueMap(defaultArgumentMap)
                                                                    .build();
                        
                        log.debug("defaultMap={}", defaultMap);
                        model.addAttribute("defaultMap", defaultMap);
                        
                      });
      
    }
    return "algorithms/algo-model-training";
  }
  
  private List<ParamForThymeleaf> buildParamForThymeleaf(List<AlgoParameterOutput> paramList) {
    List<ParamForThymeleaf> result = new ArrayList<ParamForThymeleaf>();
    for (AlgoParameterOutput param : paramList) {
      BoostrapFormatType boostrapFormat = buildBoostrapFormat(param.getType(), param.getRange());
      ParamForThymeleaf paramTemp = ParamForThymeleaf.builder()
                                                     .name(param.getName())
                                                     .type(param.getType())
                                                     .format(boostrapFormat.getFormat())
                                                     .range(boostrapFormat.getRangeList())
                                                     .value(param.getDefaultValue())
                                                     .build();
      result.add(paramTemp);
    }
    log.debug("buildParamForThymeleaf_result={}", result);
    return result;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class BoostrapFormatType {
    List<String> rangeList;
    String format;
  }
  
  private BoostrapFormatType buildBoostrapFormat(String type, String range) {
    BoostrapFormatType result = BoostrapFormatType.builder()
                                                  .build();
    if (StringUtils.equals("", range)) {
      result.setFormat("text");// <input type =text>
      result.setRangeList(Collections.emptyList());
      log.debug("resultForText={}", result);
      return result;
    } else {
      if (StringUtils.equals("enum", type) || StringUtils.equals("boolean", type)) {
        // SELECT OPTION
        List<String> rangeListTemp = splitRangeToList(range);
        result.setFormat("select");
        result.setRangeList(rangeListTemp);
        log.debug("resultForSelect={}", result);
      } else if ((StringUtils.equals("int", type)) || (StringUtils.equals("float", type))) {
        // FIXME 有範圍限制的數值形態//interval
        List<String> rangeListTemp = splitRangeToList(range);
        result.setFormat("text");
        result.setRangeList(rangeListTemp);
        log.debug("resultForText={}", result);
      } else {
        log.debug("missing case");
        result.setFormat("text");// <input type =text>
        result.setRangeList(Collections.emptyList());
        log.debug("resultForText={}", result);
      }
    }
    return result;
  }
  
  List<String> splitRangeToList(String range) {
    String[] splitRange = range.split(",");
    List<String> rangeListTemp = new ArrayList<String>();
    for (String s : splitRange) {
      rangeListTemp.add(s);
    }
    return rangeListTemp;
  }
  
  /**
   * 
   * Model Training Page--Step 3 Model Training
   */
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoAlgoParam {
    String fileId;
    String modelMethod;
    String projectId;
    String modelName;
    Map<String, String> arguments;
    
  }
  
  @PostMapping(path = PATH, params = "action=Train")
  public String doModelTraining(@RequestParam Map<String, String> allRequestParams, AlgoInputFormat defaultMap,
      DoAlgoParam doAlgoParam, Principal principal, Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}doModelTraining,allRequestParams={}", PATH, allRequestParams);
    final Optional<CsvInfo> _csvInfo = userInfoService.getUserProjectCsv(userId, doAlgoParam.getProjectId(),
        doAlgoParam.getFileId());
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    } else {
      CsvInfo csvInfo = _csvInfo.get();
      model.addAttribute("csvInfo", csvInfo);
      
      Map<String, String> _arguments = buildTrainingInputArguments(allRequestParams);
      log.debug("_arguments={}", _arguments);
      
      PostDoAlgoInput trainingInput = PostDoAlgoInput.builder()
                                                     .fileId(csvInfo.getFileId())
                                                     .projectType(csvInfo.getProjectType())
                                                     .modelMethod(allRequestParams.get("modelMethod"))
                                                     .modelName(allRequestParams.get("modelName"))
                                                     .arguments(_arguments)
                                                     .build();
      
      buildInputCache(model, csvInfo, trainingInput);
      log.debug("doAlogoInput={}", trainingInput);
      trainingInputCache.put(doAlgoParam.getFileId(), trainingInput);
      model.addAttribute("actoin", "Train");
      model.addAttribute("trainingInput", trainingInput);
      Optional<PostDoAlgoOutput> _res = algorithmService.postDoAlgoModelTraining(AnalysisServer.PYTHON, trainingInput);
      if (_res.isPresent()) {
        PostDoAlgoOutput res = _res.get();
        // Spring data
        ModelInfo storedModelInfo = storeDBandResetInfo(principal.getName(), csvInfo.getProjectId(), res.getModelId());
        if ("ok".equalsIgnoreCase(res.getStatus())) {
          // 之後補充：訓練資料有NaN時出現的錯誤訊息
          model.addAttribute("modelId", storedModelInfo.getModelId());
          model.addAttribute("projectType", csvInfo.getProjectType());
          log.debug("projectTypeprojectTpye={}", csvInfo.getProjectType());
          log.debug("{}[action=Apply ModelTrainingOutputInModelInfo ={},", PATH, storedModelInfo);
          
        }
        
      }
      final PreviewParam previewParam;
      
      /**
       * ---<Regression> PreviewParam
       * ----的xy坐標設定--------------------------------------
       * x可以是所有csv檔案的欄位名稱(columnName) y 只能是所選定的label
       */
      if (StringUtils.equals("regression", csvInfo.getProjectType())) {
        log.debug("regression_preview");
        if (csvInfo.getColumnNames()
                   .size() >= 2) {
          previewParam = PreviewParam.builder()
                                     .x(csvInfo.getColumnNames()
                                               .get(0))
                                     .y(csvInfo.getLabel())
                                     .build();
        } else {
          log.error("PreviewParam error: {}", "ColumnName size < 2");
          previewParam = PreviewParam.builder()
                                     .build();
        }
      } else {
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
      }
      
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("action", "Train");
      model.addAttribute("sidebarActiveId", "sidebar-model-training");
      
    }
    return "algorithms/algo-model-training";
  }
  
  /**
   * 
   * Model Preview
   */
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PreviewParam {
    String x;
    String y;
    String projectId;
    String fileId;
    String modelId;
  }
  
  @PostMapping(path = PATH, params = "action=Preview")
  public String preview(PreviewParam param, Principal principal, Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=Preview] fileId={}, x={}, y={}", PATH, param.getFileId(), param.getX(), param.getY());
    
    final Optional<CsvInfo> _csvInfo = userInfoService.getUserProjectCsv(userId, param.getProjectId(),
        param.getFileId());
    if (!_csvInfo.isPresent()) {
      redirectAttributes.addFlashAttribute("modelTrainingMessage", "error fileId");
      return "redirect:/";
    }
    
    _csvInfo.ifPresent(csvInfo -> {
      model.addAttribute("csvInfo", csvInfo);
      
      buildInputCache(model, csvInfo, trainingInputCache.get(param.getFileId()));// FIXME
      final PreviewParam previewParam = PreviewParam.builder()
                                   .x(param.getX())
                                   .y(param.getY())
                                   .build();
     
      model.addAttribute("previewParam", previewParam);
      model.addAttribute("projectType", csvInfo.getProjectType());
      final PostModelPreviewInput input = PostModelPreviewInput.builder()
                                                               .server(csvInfo.getServer())
                                                               .fileId(csvInfo.getFileId())
                                                               .modelId(param.getModelId())
                                                               .xAxis(param.getX())
                                                               .yAxis(param.getY())
                                                               .build();
      
      Optional<PostModelPreviewOutput> _res = modelTrainingService.postModelPreview(input);
      _res.ifPresent(res -> {
        log.debug("trainingModelPreview_imgUrl={}", res.getImageUrl());
        
        model.addAttribute("imageUrl", res.getImageUrl());
        
      });
      
      final PredictParam predictParam = PredictParam.builder()
                                                    .fileId(csvInfo.getFileId())
                                                    .modelId(param.getModelId())
                                                    .projectId(csvInfo.getProjectId())
                                                    .labelColumn(csvInfo.getLabel())
                                                    .build();
      predictAfterTraining(csvInfo, predictParam, model, redirectAttributes);
      
    });
    
    model.addAttribute("trainingInput", trainingInputCache.get(param.getFileId()));
    model.addAttribute("modelId", param.getModelId());
    model.addAttribute("action", "Preview");
    model.addAttribute("sidebarActiveId", "sidebar-model-training");
    
    return "algorithms/algo-model-training";
  }
  
  @PostMapping(path = PATH, params = "action=Cancel")
  public String cancel(Model model, String projectId) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + PROJECT + "/" + projectId;
  }
  
  Map<String, String> buildTrainingInputArguments(Map<String, String> allRequestParams) {
    Map<String, String> _arguments = new LinkedHashMap<>();
    List<String> keyValue = new ArrayList<String>();
    List<String> keyName = new ArrayList<String>();
    for (Entry<String, String> entry : allRequestParams.entrySet()) {
      keyName.add(entry.getKey());
      keyValue.add(entry.getValue());
    }
    log.debug("keyName={},size={}", keyName, keyName.size());
    log.debug("keyValue={},size={}", keyValue, keyValue.size());
    if (keyName.contains("action")) {
      int lastIndex = keyName.indexOf("action");
      log.debug("lastIndex={},lastIndex");
      for (int index = 3; index < (lastIndex - 1); index++) {
        _arguments.put(keyName.get(index), keyValue.get(index));
      }
      log.debug("buildTrainingInputArguments_arguments={}", _arguments);
    } else {
      log.debug("action missing");
    }
    return _arguments;
    
  }
  
  private ModelInfo storeDBandResetInfo(String userId, String projectId, String ModelId) {
    // 1. buildModelInfoStoreToDB(index = null)
    ModelInfo result = null;
    if (modelInfoService.buildModelInfoStoreToDB(ModelId)) {
      log.debug("after build db and set index_ModelList={}",
          userInfoService.getUserProjectModelList(userId, projectId));
      result = userInfoService.getUserProjectModel(userId, projectId, ModelId)
                              .get();
      log.debug("storeDBandResetInfo result = {}", result);
    }
    return result;
  }
  
  private List<ParamForThymeleaf> buildUserParamForThymeleaf(List<AlgoParameterOutput> paramList,
      PostDoAlgoInput trainingInput) {
    log.debug("trainingInput_argument={}", trainingInput.getArguments());
    List<String> paramValue = new ArrayList<String>();
    List<ParamForThymeleaf> result = new ArrayList<ParamForThymeleaf>();
    for (AlgoParameterOutput param : paramList) {
      BoostrapFormatType boostrapFormat = buildBoostrapFormat(param.getType(), param.getRange());
      ParamForThymeleaf paramTemp = ParamForThymeleaf.builder()
                                                     .name(param.getName())
                                                     .type(param.getType())
                                                     .format(boostrapFormat.getFormat())
                                                     .range(boostrapFormat.getRangeList())
                                                     .value(param.getDefaultValue())
                                                     .build();
      result.add(paramTemp);
    }
    for (Entry<String, String> entry : trainingInput.getArguments()
                                                    .entrySet()) {
      paramValue.add(entry.getValue());
    }
    log.debug("paramValue={}", paramValue);
    for (int i = 0; i < result.size(); i++) {
      result.get(i)
            .setValue(paramValue.get(i));
    }
    
    log.debug("buildUserParamForThymeleaf_result={}", result);
    return result;
  }
  
  private void buildInputCache(Model model, CsvInfo csvInfo, PostDoAlgoInput trainingInput) {
    algorithmService.getAlgoParameters(csvInfo.getServer(), trainingInput.getModelMethod())
                    .ifPresent(output -> {
                      List<ParamForThymeleaf> paramToShow = buildUserParamForThymeleaf(output.getArgumentsDef(),
                          trainingInput);
                      log.debug("after_buildUserParamForThymeleaf_paramToShow={}", paramToShow);
                      model.addAttribute("parameters", paramToShow);
                      
                      Map<String, ParamForThymeleaf> userInputTypeMap = new LinkedHashMap<>();
                      for (ParamForThymeleaf userParamForThyme : paramToShow) {
                        userInputTypeMap.put(userParamForThyme.getName(), userParamForThyme);
                      }
                      
                      AlgoInputFormat userInputMap = AlgoInputFormat.builder()
                                                                    .projectType(csvInfo.getProjectType())
                                                                    .fileId(csvInfo.getFileId())
                                                                    .modelMethod(trainingInput.getModelMethod())
                                                                    .modelName(trainingInput.getModelName())
                                                                    .typeMap(userInputTypeMap)
                                                                    .build();
                      log.debug("userInputMap_defaultMap={}", userInputMap);
                      model.addAttribute("defaultMap", userInputMap);
                    });
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PredictParam {
    String fileId;
    String modelId;
    String projectId;
    String labelColumn;
  }
  
  private void predictAfterTraining(CsvInfo csvInfo, PredictParam param, Model model,
      RedirectAttributes redirectAttributes) {
    final PostModelPredictInput predictInput = PostModelPredictInput.builder()
                                                                    .server(csvInfo.getServer())
                                                                    .modelId(param.getModelId())
                                                                    .fileId(param.getFileId())
                                                                    .projectType(csvInfo.getProjectType())
                                                                    .build();
    log.debug("predictInput={}", predictInput);
    switch (csvInfo.getProjectType()) {
      // TODO case abnormal
      case "regression":
        log.info("regression_predictInput: {}", predictInput);
        Optional<PostRegressionModelPredictOutput> _regression_output = modelPredictionService.postRegressionModelPredict(
            predictInput);
        
        if (_regression_output.isPresent()) {
          log.info("regressionPredictResult: {}", _regression_output);
          PostRegressionModelPredictOutput predictOutput = _regression_output.get();
          log.debug("regressionPredictOutputToThymleaf: {}", predictOutput);
          model.addAttribute("predictOutput", predictOutput);
        }
        break;
      
      case "classification":
        log.info("classification_predictInput: {}", predictInput);
        Optional<PostClassificationModelPredictOutput> _classification_output = modelPredictionService.postClassificationModelPredict(
            predictInput);
        if (_classification_output.isPresent()) {
          log.info("ClassificationPredictResult: {}", _classification_output);
          PostClassificationModelPredictOutput predictOutput = _classification_output.get();
          if ((KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                         .getClassificationReport())) != null
              && (KnnPredictionUtil.classificationReportOutput(predictOutput.getPerformance()
                                                                            .getConfusionMatrix())) != null) {
            String[][] classificationReportArray = KnnPredictionUtil.classificationReportOutput(
                predictOutput.getPerformance()
                             .getClassificationReport());
            String[][] confusionMatrixArray = KnnPredictionUtil.classificationReportOutput(
                predictOutput.getPerformance()
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
        break;
      default:
        log.info("type: {}", csvInfo.getProjectType());
        break;
    }
    return;
  }
  
}
