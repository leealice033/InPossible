package com.insnergy.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.CsvInfoService;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.DataPreProcessService;
import com.insnergy.service.rest.DataPreProcessService.PostPreProcessSaveResponse;
import com.insnergy.service.rest.DataPreProcessService.PostPreProcessViewerInput;
import com.insnergy.service.rest.DataViewerService;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.CsvInfo.CsvInfoRow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DataPreProcessController {
  
  private static final String PATH = "/data-preprocess";
  // private static final String URL_HEADER = "http://127.0.0.1:8002/";
  private final UserInfoService userService;
  private final DataPreProcessService dataPreProcessService;
  private final DataViewerService dataViewerService;
  private final CsvInfoService csvInfoService;
  private String selectFeatureImg = null;
  
  public DataPreProcessController(UserInfoService userService, DataPreProcessService dataPreProcessService,
      DataViewerService dataViewerService, CsvInfoService csvInfoService) {
    this.userService = userService;
    this.dataPreProcessService = dataPreProcessService;
    this.dataViewerService = dataViewerService;
    this.csvInfoService = csvInfoService;
  }
  
  @ModelAttribute("sidebarActiveId")
  public String populateSidebarActiveId() {
    return "sidebar-data-preprocessing";
  }
  
  @GetMapping("/{projectId:.+}" + PATH + "/{fileId:.+}")
  public String toDataPreprocessPage(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=Data Preprocess] userId={}, projectId={}, fileId={}", PATH, userId, projectId, fileId);
    
    if (fileId == null) {
      redirectAttributes.addFlashAttribute("dataPreprocessMessage", "error: fileId is null");
      return "redirect:" + ProjectController.PATH + "/" + projectId;
    } else {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   CsvInfo cloneInfo = SerializationUtils.clone(csvInfo);
                   cloneInfo.setColumnNames(makeOnlyNumberColumnNames(csvInfo));
                   log.debug("clonInfo={}", cloneInfo);
                   model.addAttribute("csvInfo", cloneInfo);
                 });
      
      model.addAttribute("intervalNumber", 100);
      model.addAttribute("previewParam", new PreviewParam());
      
      return "data-preprocess";
    }
  }
  
  private List<String> makeOnlyNumberColumnNames(CsvInfo csvInfo) {
    log.debug("csvInfo makeOnlyNumberColumnNames={}", csvInfo);
    
    List<String> numberColumnNames = new ArrayList<>();
    String[] columnNameArray = csvInfo.getColumnNames()
                                      .toArray(new String[] {});
    List<CsvInfoRow> _row = csvInfo.getRowValues();
    if (_row != null) {
      String[] rowValueArray = _row.get(0)
                                   .getRowValue()
                                   .toArray(new String[] {});
      for (int i = 0; i < columnNameArray.length; i++) {
        if (NumberUtils.isCreatable(rowValueArray[i])) {
          numberColumnNames.add(columnNameArray[i]);
        }
      }
    } else {
      numberColumnNames = Collections.emptyList();
    }
    return numberColumnNames;
  }
  
  // FIXME chose feature show data viewer
  @PostMapping(path = PATH, params = "action=Select Feature")
  public String selectFeature(String projectId, String fileId, Principal principal, String featureName, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=Select] userId={}, projectId={}, fileId={}, featureName={}", PATH, userId, fileId, projectId,
        featureName);
    
    userService.getUserProjectCsv(userId, projectId, fileId)
               .ifPresent(csvInfo -> {
                 
                 dataViewerService.getDataViewerByColumn(csvInfo.getServer(), csvInfo.getFileId(), featureName)
                                  .ifPresent(callDataViewerRes -> {
                                    String imgPath = callDataViewerRes.getImagePath();
                                    log.debug("featureImgPath={},featureName={}", imgPath, featureName);
                                    // String imgUrl = URL_HEADER + imgPath;
                                    log.debug("FINALfeatureImgPathFIX={},featureName={}", imgPath, featureName);
                                    selectFeatureImg = imgPath;
                                    model.addAttribute("featureImgPath", imgPath);
                                  });
                                  
                 CsvInfo cloneInfo = SerializationUtils.clone(csvInfo);
                 cloneInfo.setColumnNames(makeOnlyNumberColumnNames(csvInfo));
                 log.debug("action=Select Feature_cloneInfo: {}", cloneInfo);
                 model.addAttribute("csvInfo", cloneInfo);
               });
    
    model.addAttribute("action", "SelectFeature");
    model.addAttribute("previewParam", PreviewParam.builder()
                                                   .fileId(fileId)
                                                   .featureName(featureName)
                                                   .build());
    log.debug("action=Select Feature_previewParam: {}", PreviewParam.builder()
                                                                    .fileId(fileId)
                                                                    .featureName(featureName)
                                                                    .build());
    return "data-preprocess";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PreviewParam {
    String fileId;
    String featureName;
    Boolean filterMissingValue;
    Integer filterOutlier;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PreprocessSaveParam {
    String newFileName;
    String Normalize;
    List<String> feature_list;
  }
  
  @PostMapping(path = PATH, params = "action=Preview")
  public String preprocessPeview(String projectId, String fileId, Principal principal, PreviewParam param, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=Preview] param={}", PATH, param);
    
    userService.getUserProjectCsv(userId, projectId, param.getFileId())
               .ifPresent(csvInfo -> {
                 

                 model.addAttribute("previewParam", param);
                 final PostPreProcessViewerInput dataViewerInput = PostPreProcessViewerInput.builder()
                                                                                            .featureName(
                                                                                                param.getFeatureName())
                                                                                            .filterStd(
                                                                                                param.getFilterOutlier())
                                                                                            .missingValue(
                                                                                                param.getFilterMissingValue())
                                                                                            .build();
                 
                 dataPreProcessService.postPreProcessDataViewer(csvInfo.getServer(), csvInfo.getFileId(),
                     dataViewerInput)
                                      .ifPresent(res -> {
                                        log.debug("postPreProcessDataViewerOutput: {}", res);
                                        log.debug("postPreProcessDataViewerOutput_tempId: {}", res.getTmpFileId());
                                        dataViewerService.getDataViewerByColumn(csvInfo.getServer(), res.getTmpFileId(),
                                            res.getFeatureName())
                                                         .ifPresent(callDataViewerRes -> {
                                                           String imgPath = callDataViewerRes.getImagePath();
                                                           log.debug("previewImgPath={},featureName={}", imgPath,
                                                               res.getFeatureName());
                                                           // String imgUrl =
                                                           // URL_HEADER +
                                                           // imgPath;
                                                           log.debug("FINALpreviewImgPathFix={},featureName={}",
                                                               imgPath, res.getFeatureName());
                                                           model.addAttribute("previewImgPath", imgPath);
                                                         });
                                                         
                                      });
                 CsvInfo cloneInfo = SerializationUtils.clone(csvInfo);
                 cloneInfo.setColumnNames(makeOnlyNumberColumnNames(csvInfo));
                 log.debug("action=Preview_cloneInfo: {}", cloneInfo);
                 model.addAttribute("csvInfo", cloneInfo);
               });
    model.addAttribute("featureImgPath", selectFeatureImg);
    model.addAttribute("action", "Preview");
    return "data-preprocess";
  }
  
  @PostMapping(path = PATH, params = "action=Cancel")
  public String cancel(String projectId, Model model) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  

  @PostMapping(path = PATH, params = "action=Save")
  public String save(String projectId, String fileId, Principal principal, String normalizeAlgorithm,
      String newFileName, String normalizeFeatureName, Model model, RedirectAttributes redirectAttributes) {
    final String userId = principal.getName();
    log.debug("normalize_featureName={}", normalizeFeatureName);
    try {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   List<String> featureList = Arrays.asList();
                   if (normalizeFeatureName != null) {
                     featureList = Arrays.asList(normalizeFeatureName.split(","));
                     
                   }
                   log.debug("normalize_featureList={}", featureList);
                   Optional<PostPreProcessSaveResponse> _res = dataPreProcessService.postPreProcessSave(
                       csvInfo.getServer(), csvInfo.getFileId(), newFileName, normalizeAlgorithm, featureList);
                   
                   log.debug("{}[action=Save] fileId={}", PATH, fileId);
                   if (_res.isPresent()) {
                     // TODO DB
                     // csvInfo -> call python get csv by fileId
                     storeDBandResetInfo(userId, projectId, _res.get()
                                                                .getNew_file_id());
                     
                     redirectAttributes.addFlashAttribute("dataPreprocessMessage",
                         
                         String.format("Data preprocess success: %s (from %s)", newFileName, csvInfo.getFileName()));
                     userService.refreshUserProject(userId, projectId);
                   } else {
                     redirectAttributes.addFlashAttribute("dataPreprocessMessage",
                         String.format("Data preprocess fail: %s", csvInfo.getFileName()));
                   }
                 });
    } catch (Exception e) {
      final String message = ExceptionUtils.getMessage(e);
      log.error("postCsv error: {}", message, e);
      redirectAttributes.addFlashAttribute("dataPreprocessMessage", String.format("Data preprocess fail: %s", message));
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