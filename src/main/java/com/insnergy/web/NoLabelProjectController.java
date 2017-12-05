package com.insnergy.web;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.CsvInfoService;
import com.insnergy.service.FileUploadService;
import com.insnergy.service.ServerStatusCache;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.CsvService.PostCsvInput;
import com.insnergy.service.rest.CsvService.PostCsvOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.Stages;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(NoLabelProjectController.PATH)
@Slf4j
public class NoLabelProjectController {
  static final String PATH = "/no-label-project";
  
  private static final String UPLOAD_NONLABEL_FILE_PATH = "/no-label-csv-upload";
  public static final String DELETE_FILE_PATH = "/csv-delete";
  public static final String SET_LABEL = "/csv-label";
  private static final String DATA_PRE_PROCESS_FILE_LIST = "dataPreProcessFileList";
  private static final String FEATURE_SELECTION_FILE_LIST = "featureSelectionFileList";
  private static final String MODEL_TRAINING_FILE_LIST = "modelTrainingFileList";
  private static final String MODEL_PREDICTION_FILE_LIST = "modelPredictionFileList";
  
  private static final Map<String, String> STAGE_TO_FILE_LIST_MAPPING;
  
  static {
    Map<String, String> map = new HashMap<>();
    map.put(DATA_PRE_PROCESS_FILE_LIST, Stages.DATA_PREPROCESS);
    map.put(FEATURE_SELECTION_FILE_LIST, Stages.FEATURE_SELECTION);
    map.put(MODEL_TRAINING_FILE_LIST, Stages.MODEL_TRAINING);
    map.put(MODEL_PREDICTION_FILE_LIST, Stages.MODEL_PREDICTION);
    STAGE_TO_FILE_LIST_MAPPING = Collections.unmodifiableMap(map);
  }
  
  private final UserInfoService userService;
  private final ServerStatusCache serverStatusCache;
  private final FileUploadService fileUploadService;
  private final CsvService csvService;
  private final CsvInfoService csvInfoService;
  
  public NoLabelProjectController(UserInfoService userService, ServerStatusCache serverStatusCache,
      FileUploadService fileUploadService, CsvService csvService, CsvInfoService csvInfoService) {
    this.userService = userService;
    this.serverStatusCache = serverStatusCache;
    this.fileUploadService = fileUploadService;
    this.csvService = csvService;
    this.csvInfoService = csvInfoService;
  }
  
  @ModelAttribute("sidebarActiveId")
  public String populateSidebarActiveId() {
    return "sidebar-data-input";
  }
  
  @ModelAttribute("pythonServerStatus")
  public Boolean populatePythonServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.PYTHON);
  }
  
  @ModelAttribute("rServerStatus")
  public Boolean populateRServerStatus() {
    return serverStatusCache.getServerStatus(AnalysisServer.R);
  }
  
  @GetMapping("/refresh/{projectId:.+}")
  public String refreshCsvFileInfo(@PathVariable String projectId, Model model, Principal principal) {
    log.debug("refreshCsvFileInfo");
    
    userService.refreshUserProject(principal.getName(), projectId);
    
    return "redirect:" + PATH + "/" + projectId;
  }
  
  @GetMapping("/{projectId:.+}")
  public String toNoLabelProjectPage(@PathVariable String projectId, Model model, Principal principal) {
    final String userId = principal.getName();
    log.debug("user={}, project={}", userId, projectId);
    
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo project = userInfo.getProjects()
                                    .get(projectId);
      
      userService.refreshUserProject(userId, project.getId());
      log.debug("toProjectPage_project={}", project);
      
      model.addAttribute("projectTypeTemp", project.getType());
      log.debug("toProject projectTypeTemp={}", project.getType());
      
      if (project.getCsvs()
                 .size() == 0) {
        
        log.debug("csvSize={}", project.getCsvs()
                                       .size());
        log.debug("toProject Page_project_after_refresh={}", project);
        userService.refreshUserProject(userId, project.getId());
      }
      
      log.debug("toProject Page_project_after_refresh={}", project);
      model.addAttribute("project", project);
      STAGE_TO_FILE_LIST_MAPPING.forEach((fileListName, stage) -> {
        model.addAttribute(fileListName, project.getCsvs()
                                                .stream()
                                                .filter(csv -> StringUtils.equals(csv.getStage(), stage))
                                                .collect(Collectors.toList()));
      });
      
    });
    return "no-label-project";
  }
  
  // TODO nonlabelCsvUpload
  @PostMapping(path = "/{projectId:.+}" + UPLOAD_NONLABEL_FILE_PATH, params = "action=Upload")
  public String nonlabelCsvUpload(@PathVariable String projectId, String server, String stage, String projectType,
      String label, String fileName, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes,
      Model model, Principal principal) throws IOException {
    log.debug(
        "action=[Nonlabel-Upload]_projectId={}, fileName={}, server={}, stage={},Uploaded_file_projectType={}, Uploaded_file_label={}",
        projectId, fileName, server, stage, projectType, label);
    
    final String finalStage = fixStage(stage);
    model.addAttribute("stage", finalStage);
    log.debug("action=UploadButNotLabel_FinalStage={}_ProjectType={}", finalStage, projectType);
    
    // check stage
    
    final String userId = principal.getName();
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo _project = userInfo.getProjects()
                                     .get(projectId);
      //
      
      boolean storeFileSuccess = false;
      try {
        fileUploadService.store(file);
        storeFileSuccess = true;
        log.debug("storeFileSuccess={}", storeFileSuccess);
        log.debug("store_fileName={}", file.getOriginalFilename());
        
        log.debug("store_path={}", fileUploadService.load(file.getOriginalFilename()));
        
      } catch (Exception e) {
        final String message = ExceptionUtils.getMessage(e);
        log.error("store file error: {}", message);
        redirectAttributes.addFlashAttribute("csvFileUploadMessage", String.format("File upload error: %s", message));
      }
      if (storeFileSuccess) {
        // store file local success
        model.addAttribute("projectTypeTemp", _project.getType());
        try {
          final PostCsvInput input = PostCsvInput.builder()
                                                 .server(AnalysisServer.valueOf(server))
                                                 .csvFileName(file.getOriginalFilename())
                                                 .projectId(projectId)
                                                 .projectType(projectType)
                                                 .userId(userId)
                                                 .label("")
                                                 .stage(finalStage)
                                                 .build();
          // Call PYTHON postCsv
          Optional<PostCsvOutput> _res = csvService.postCsv(input);
          if (_res.isPresent()) {
            // csvInfo -> call python get csv by fileId
            storeDBandResetInfo(userId, projectId, _res.get()
                                                       .getFileId());
          }
          log.debug("action=SaveLabel_postCsv={}", _res);
          
          if (_res.isPresent()) {
            csvService.getCsv(AnalysisServer.valueOf(server), _res.get()
                                                                  .getFileId())
                      .ifPresent(output -> {
                        userService.findUserInfoById(userId)
                                   .ifPresent(findingUser -> {
                                     final ProjectInfo projectInfo = findingUser.getProjects()
                                                                                .get(projectId);
                                     
                                     List<CsvInfo> csvs = new ArrayList<>();
                                     final List<CsvInfo> oriCsvs = projectInfo.getCsvs();
                                     if (oriCsvs != null) {
                                       csvs.addAll(oriCsvs);
                                     }
                                     
                                     final List<CsvInfo> newCsvList = output.getCsvList();
                                     if (newCsvList != null) {
                                       csvs.addAll(newCsvList);
                                     }
                                     projectInfo.setCsvs(csvs);
                                   });
                      });
          } else {
            redirectAttributes.addFlashAttribute("csvFileUploadMessage",
                String.format("% server upload %s fail", server, fileName));
          }
        } catch (Exception e) {
          final String message = ExceptionUtils.getMessage(e);
          log.error("postCsv error: {}", message, e);
          redirectAttributes.addFlashAttribute("csvFileUploadMessage",
              String.format("% server download %s fail: %s", server, fileName, message));
        }
        
      }
      // end if
    });
    
    return toNoLabelProjectPage(projectId, model, principal);
  }
  
  public static String getDeleteFilePath(String projectId, String fileId) {
    return String.format("%s/%s%s/%s", PATH, projectId, DELETE_FILE_PATH, fileId);
  }
  
  @GetMapping("/{projectId:.+}" + DELETE_FILE_PATH + "/{fileId:.+}")
  public String deleteFile(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model) {
    log.info("[deleteFile] projectId={}, fileId={}", projectId, fileId);
    final String userId = principal.getName();
    
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      final ProjectInfo project = userInfo.getProjects()
                                          .get(projectId);
      
      project.getCsvs()
             .stream()
             .filter(csv -> StringUtils.equals(csv.getFileId(), fileId))
             .findAny()
             .ifPresent(csv -> {
               csvService.deleteCsv(csv.getServer(), fileId)
                         .ifPresent(output -> {
                           if (StringUtils.equalsIgnoreCase(output.getStatus(), "ok")) {
                             project.getCsvs()
                                    .remove(csv);
                           }
                         });
             });
             
    });
    model.addAttribute("action", "");
    return "redirect:" + PATH + "/" + projectId;
  }
  
  private String fixStage(String stage) {
    String _stage = stage.replaceAll(",", "");
    if (_stage.equals("data-preprocess,model-prediction")) {
      _stage = _stage.replace("data-preprocess,", "");
    } else if (_stage.equals("data-preprocess,data-preprocess")) {
      _stage = _stage.replaceFirst("data-preprocess,", "");
    } else if (_stage.equals("feature-selection,feature-selection")) {
      _stage = _stage.replaceFirst("feature-selection,", "");
    } else if (_stage.equals("model-training,model-training")) {
      _stage = _stage.replaceFirst("model-training,", "");
    } else if (_stage.equals("model-prediction,model-prediction")) {
      _stage = _stage.replaceFirst("model-prediction,", "");
    }
    
    log.debug("action=UploadButNotLabel_stage={}", _stage);
    return _stage;
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
