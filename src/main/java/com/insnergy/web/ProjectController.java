package com.insnergy.web;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.insnergy.domain.CsvEntity;
import com.insnergy.repo.CsvEntityRepo;
import com.insnergy.service.CsvInfoService;
import com.insnergy.service.FileUploadService;
import com.insnergy.service.ServerStatusCache;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.CsvService.PostCsvInput;
import com.insnergy.service.rest.CsvService.PostCsvOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.CsvFileUtil;
import com.insnergy.util.Stages;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(ProjectController.PATH)
@Slf4j
public class ProjectController {
  
  static final String PATH = "/project";
  
  private static final String UPLOAD_FILE_PATH = "/csv-upload";
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
  private final CsvEntityRepo csvRepo;
  
  public ProjectController(UserInfoService userService, ServerStatusCache serverStatusCache,
      FileUploadService fileUploadService, CsvService csvService, CsvInfoService csvInfoService,
      CsvEntityRepo csvRepo) {
    this.userService = userService;
    this.serverStatusCache = serverStatusCache;
    this.fileUploadService = fileUploadService;
    this.csvService = csvService;
    this.csvInfoService = csvInfoService;
    this.csvRepo = csvRepo;
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
  public String toProjectPage(@PathVariable String projectId, Model model, Principal principal) {
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
    return "project";
  }
  
  /**
   * projectType =<clustering> <abnormal>
   * 
   * @param projectId
   * @param server
   * @param stage
   * @param projectType
   * @param label
   * @param fileName
   * @param file
   * @param redirectAttributes
   * @param model
   * @param principal
   * @return
   * @throws IOException
   */
  // TODO nonlabelCsvUpload
  @PostMapping(path = "/{projectId:.+}" + UPLOAD_NONLABEL_FILE_PATH, params = "action=Upload")
  public String nonlabelCsvUpload(@PathVariable String projectId, String server, String stage, String projectType,
      String label, String fileName, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes,
      Model model, Principal principal) throws IOException {
    log.debug(
        "action=[Nonlabel-Upload]_projectId={}, fileName={}, server={}, stage={},Uploaded_file_projectType={}, Uploaded_file_label={}",
        projectId, fileName, server, stage, projectType, label);
    final String finalStage = fixStage(stage);
    log.debug("action=UploadButNotLabel_FinalStage={}_ProjectType={}", finalStage, projectType);
    model.addAttribute("stage", finalStage);
    
    // check stage
    
    final String userId = principal.getName();
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo _project = userInfo.getProjects()
                                     .get(projectId);
      
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
            log.debug("nonlabelCsvUpload={}", projectType);
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
    
    return toProjectPage(projectId, model, principal);
  }
  
  /**
   * 
   * @param projectId
   * @param server
   * @param stage
   * @param projectType
   * @param label
   * @param fileName
   * @param file
   * @param redirectAttributes
   * @param model
   * @param principal
   * @return
   * @throws IOException
   */
  // FIXME
  @PostMapping(path = "/{projectId:.+}" + UPLOAD_FILE_PATH, params = "action=Upload")
  public String csvLabelSetting(@PathVariable String projectId, String server, String stage, String projectType,
      String label, String fileName, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes,
      Model model, Principal principal) throws IOException {
    // not csv format
    if (!file.getOriginalFilename()
             .endsWith(".csv")) {
      redirectAttributes.addFlashAttribute("csvFileUploadMessage",
          String.format("File upload error: %s", "not csv format"));
      model.addAttribute("invalidCsv", "Not a csv file! Please check your file is csv format!");
      return toProjectPage(projectId, model, principal);
    }
    log.debug("check_stage={}", stage);
    log.debug(
        "action=[Upload]_projectId={}, fileName={}, server={}, stage={},Uploaded_file_projectType={}, Uploaded_file_label={}",
        projectId, fileName, server, stage, projectType, label);
    String _stage = stageFormatCheck(stage);
    log.debug("action=UploadButNotLabel_stage={}_ProjectType={}", _stage, projectType);
    model.addAttribute("stage", _stage);
    
    final String userId = principal.getName();
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo _project = userInfo.getProjects()
                                     .get(projectId);
      
      model.addAttribute("projectTypeTemp", _project.getType());
    });
    
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
      fileName = file.getOriginalFilename();
      log.debug("storeFileSuccess_fileName={}", fileName);
      model.addAttribute("fileName", file.getOriginalFilename());
      String[] columnNames = CsvFileUtil.getCsvColumnNames(fileName);
      log.debug("columnNames[0].equals={}", columnNames[0]);
      if (columnNames[0].equals("-1")) {
        System.out.println("has ';'");// have ';'
        String empty = null;
        _stage = empty;
        model.addAttribute("stage", _stage);
        model.addAttribute("invalidCsv", "CSV Invalid, please choose csv file and upload again!");
        log.debug("invalidCsv_reason={}", columnNames[0]);
        return toProjectPage(projectId, model, principal);
      }
      model.addAttribute("action", "Upload");
      
      if (StringUtils.equals("abnormal-detection", projectType) && !StringUtils.equals("model-prediction", _stage)) {
        log.debug("enter abnormal-detection_stage={},_projectType={}", _stage, projectType);
        return abnormalNoNeedLabel(projectId, server, _stage, projectType, label, fileName, redirectAttributes,
            principal, model);
      } else {
        // CLASSIFICATION REGRESSION CLUSTERING
        List<String> columnNameList = Arrays.asList(columnNames);
        int columnLength = columnNameList.size();
        if (StringUtils.equals("classification", projectType)) {
          // CLASSIFICATION
          List<String> validForClassfication = CsvFileUtil.classificationCase(fileName);
          columnNameList = validForClassfication;
          log.debug("FinalValidLabel={}", validForClassfication);
          log.debug("FinalValidcolumnNameList={}", columnNameList);
        } else {
          // REGRESSION CLUSTERING
          int row = 2;
          ArrayList<Integer> stringIndexArray = CsvFileUtil.checkLabel(row, columnLength, fileName, projectType);
          List<String> validLabel = new ArrayList<String>();
          for (Integer i : stringIndexArray) {
            log.debug("FindValidLabelIndex={}", i);
            log.debug("FindValidLabelValue={}", columnNameList.get(i));
            if (columnNameList.get(i) != null) {
              validLabel.add(columnNameList.get(i));
            }
          }
          
          columnNameList = validLabel;
          log.debug("FinalValidLabel={}", validLabel);
          log.debug("FinalValidcolumnNameList={}", columnNameList);
        }
        
        if (!(columnNameList.size() > 0)) {
          log.debug("label invalid");
          model.addAttribute("missingLabel", "CSV label missing , please choose csv file with valid label!");
          model.addAttribute("action", null);
          return toProjectPage(projectId, model, principal);
        }
        
        model.addAttribute("columnList", columnNameList);
        
        redirectAttributes.addFlashAttribute("csvFileUploadMessage",
            String.format("Upolad %s to [%s/%s] success", fileName, server, _stage));
      }
      
    } // storefile success
    
    return toProjectPage(projectId, model, principal);
    
  }
  
  public String abnormalNoNeedLabel(String projectId, String server, String _stage, String projectType, String label,
      String fileName, RedirectAttributes redirectAttributes, Principal principal, Model model) {
    model.addAttribute("stage", _stage);
    log.debug("action=SaveLabel_stage={}", _stage);
    String labelTemp = label.replaceAll(",", "");
    labelTemp = labelTemp.replaceAll("\"", "");
    log.debug("labelTempReplace={}", labelTemp);
    
    final String userId = principal.getName();
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo _project = userInfo.getProjects()
                                     .get(projectId);
      
      model.addAttribute("projectTypeTemp", _project.getType());
    });
    
    try {
      model.addAttribute("action", "SaveLabel");
      log.debug("fileName={}", fileName);
      final PostCsvInput input = PostCsvInput.builder()
                                             .server(AnalysisServer.valueOf(server))
                                             .csvFileName(fileName)
                                             .projectId(projectId)
                                             .projectType(projectType)
                                             .userId(userId)
                                             .stage(_stage)
                                             .label(labelTemp)
                                             .build();
      
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
                               .ifPresent(userInfo -> {
                                 final ProjectInfo projectInfo = userInfo.getProjects()
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
    model.addAttribute("action", "SaveLabel");
    return "redirect:" + PATH + "/" + projectId;
  }
  
  /**
   * 
   * @param projectId
   * @param server
   * @param stage
   * @param projectType
   * @param label
   * @param fileName
   * @param redirectAttributes
   * @param principal
   * @param model
   * @return
   */
  @PostMapping(path = "/{projectId:.+}" + UPLOAD_FILE_PATH, params = "action=Save Label")
  public String csvFileUpload(@PathVariable String projectId, String server, String stage, String projectType,
      String label, String fileName, RedirectAttributes redirectAttributes, Principal principal, Model model) {
    log.debug(
        "action=SaveLabel_projectId={}, fileName={}, server={}, stage={}, Uploaded_file_projectType={},Uploaded_file_label={}",
        projectId, fileName, server, stage, projectType, label);
    
    String _stage = stageSaveLabel(stage);
    log.debug("action=SaveLabel_stage={}", _stage);
    model.addAttribute("stage", _stage);
    String labelTemp = label.replaceAll(",", "");
    labelTemp = labelTemp.replaceAll("\"", "");
    log.debug("labelTempReplace={}", labelTemp);
    
    final String userId = principal.getName();
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      ProjectInfo _project = userInfo.getProjects()
                                     .get(projectId);
      
      model.addAttribute("projectTypeTemp", _project.getType());
    });
    
    try {
      model.addAttribute("action", "SaveLabel");
      log.debug("fileName={}", fileName);
      final PostCsvInput input = PostCsvInput.builder()
                                             .server(AnalysisServer.valueOf(server))
                                             .csvFileName(fileName)
                                             .projectId(projectId)
                                             .projectType(projectType)
                                             .userId(userId)
                                             .stage(_stage)
                                             .label(labelTemp)
                                             .build();
      
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
                               .ifPresent(userInfo -> {
                                 final ProjectInfo projectInfo = userInfo.getProjects()
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
    } catch (
    
    Exception e) {
      final String message = ExceptionUtils.getMessage(e);
      log.error("postCsv error: {}", message, e);
      redirectAttributes.addFlashAttribute("csvFileUploadMessage",
          String.format("% server download %s fail: %s", server, fileName, message));
    }
    return "redirect:" + PATH + "/" + projectId;
  }
  
  public static String getDeleteFilePath(String projectId, String fileId) {
    return String.format("%s/%s%s/%s", PATH, projectId, DELETE_FILE_PATH, fileId);
  }
  
  @GetMapping("/{projectId:.+}" + DELETE_FILE_PATH + "/{fileId:.+}")
  public String deleteFile(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model) {
    log.info("[deleteFile] projectId={}, fileId={}", projectId, fileId);
    final String userId = principal.getName();
    
    deleteFileById(fileId, projectId, userId);
    model.addAttribute("action", "");
    return "redirect:" + PATH + "/" + projectId;
  }
  
  // TODO DELETE FILE [DB]
  
  public void deleteFileById(String fileId, final String projectId, final String userId) {
    log.debug("enter_deleteFileById_fileId={},userId={}, projectId={}", fileId, userId, projectId);
    if (userService.getUserProjectCsv(userId, projectId, fileId)
                   .isPresent()) {
      // if target model is exist
      log.debug("target file to delete is ={}", userService.getUserProjectCsv(userId, projectId, fileId)
                                                           .get());
      
      Optional<ProjectInfo> _project = userService.getUserProject(userId, projectId);
      _project.ifPresent(projectInfo -> {
        log.debug("enter_deleteFileById_fileId={},projectInfo={}", fileId, _project);
        if (deleteFilePython(fileId)) {
          log.debug("PYTHON file deleted_id={}", fileId);
          if (deleteFileDB(fileId)) {
            log.debug("DB file deleted_id={}", fileId);
            projectInfo.getCsvs()
                       .remove(userService.getUserProjectCsv(userId, projectId, fileId)
                                          .get());
          }
        }
      });
    } else {
      log.debug("target file doesn't exist!,id={}", fileId);
    }
    
  }
  
  //
  private Boolean deleteFilePython(String fileId) {
    Boolean result = false;
    if (csvService.deleteCsv(AnalysisServer.PYTHON, fileId)
                  .isPresent()) {
      result = true;
      log.debug("deleteFilePython={}", result);
    }
    return result;
    
  }
  
  private Boolean deleteFileDB(String fileId) {
    Boolean result = false;
    if (csvRepo.findOneById(fileId)
               .isPresent()) {
      CsvEntity csvEntity = csvRepo.findOneById(fileId)
                                   .get();
      log.debug("findFileEntityById{}={}", fileId, csvEntity);
      csvRepo.delete(csvEntity);
      result = true;
    } else {
      log.debug("can't find File entity by Id={}", fileId);
    }
    return result;
  }
  
  private String stageFormatCheck(String stage) {
    String result = null;
    if (stage != null) {
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
      return _stage;
    }
    return result;
  }
  
  private String stageSaveLabel(String stage) {
    String result = null;
    if (stage != null) {
      String _stage = stage;
      if (_stage.equals("data-preprocess,data-preprocess")) {
        _stage = _stage.replaceFirst("data-preprocess,", "");
      } else if (_stage.equals("feature-selection,feature-selection")) {
        _stage = _stage.replaceFirst("feature-selection,", "");
      } else if (_stage.equals("model-training,model-training")) {
        _stage = _stage.replaceFirst("model-training,", "");
      } else if (_stage.equals("model-prediction,model-prediction")) {
        _stage = _stage.replaceFirst("model-prediction,", "");
        
      } else if (_stage.equals("data-preprocess,model-prediction")) {
        _stage = _stage.replace("data-preprocess,", "");
      }
      
      return _stage;
    }
    return result;
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
