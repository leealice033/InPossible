package com.insnergy.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.MakeApiService;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.service.rest.MakeApiService.PostMakeApiInput;
import com.insnergy.service.rest.MakeApiService.PostMakeApiOutput;
import com.insnergy.service.rest.UserApiService;
import com.insnergy.service.rest.UserModelService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.JsonFormatUtil;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MakeApiController {
  
  private static final String MAKE_API_PATH = "/make-api";
  
  private final UserInfoService userService;
  private final UserModelService userModelService;
  private final MakeApiService makeApiService;
  private final UserApiService userApiService;
  private final Map<String, DoMakeApiParam> makeApiInputCache;
  private final InAnalysisConfig inAnalysisConfig;
  public ApiPreviewParam showApi;
  
  public MakeApiController(UserInfoService userService, UserModelService userModelService,
      MakeApiService makeApiService, UserApiService userApiService, InAnalysisConfig inAnalysisConfig) {
    this.userService = userService;
    this.userModelService = userModelService;
    this.makeApiService = makeApiService;
    this.userApiService = userApiService;
    this.makeApiInputCache = new ConcurrentHashMap<>();
    this.inAnalysisConfig = inAnalysisConfig;
    this.showApi = new ApiPreviewParam();
  }
  
  // TODO
  @GetMapping("/{projectId:.+}" + MAKE_API_PATH + "/{modelId:.+}")
  public String toMakeApiPage(@PathVariable String projectId, @PathVariable String modelId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=GetMakeApi] userId={}, projectId={}, modelId={}", MAKE_API_PATH, userId, projectId, modelId);
    
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
      
      if (project != null && project.getModels() != null) {
        
        makeApiService.getMakeApi(AnalysisServer.PYTHON, modelId)
                      .ifPresent(makeOutput -> {
                        
                        final DoMakeApiParam.DoMakeApiParamBuilder builder = DoMakeApiParam.builder()
                                                                                           .apiName("NewAPI")
                                                                                           .apiDescription(
                                                                                               "NewAPI Description");
                        builder.inputFormats(makeOutput.getInputFormats()
                                                       .stream()
                                                       .map(inputFormat -> {
                                                         return MakeApiInputFormat.builder()
                                                                                  .featureName(
                                                                                      inputFormat.getFeatureName())
                                                                                  .userDefineFeatureName(
                                                                                      inputFormat.getFeatureName())
                                                                                  .type(inputFormat.getType())
                                                                                  .description(
                                                                                      inputFormat.getFeatureName()
                                                                                          + " Default Description")
                                                                                  .build();
                                                       })
                                                       .collect(Collectors.toList()));
                                                       
                        builder.outputFormats(makeOutput.getOutputFormats()
                                                        .stream()
                                                        .map(outputName -> {
                                                          return MakeApiOutputFormat.builder()
                                                                                    .outputName(outputName)
                                                                                    .userDefineOutpuName(
                                                                                        outputName + " Default Name")
                                                                                    .description(outputName
                                                                                        + " Default Description")
                                                                                    .build();
                                                        })
                                                        .collect(Collectors.toList()));
                                                        
                        model.addAttribute("makeApiParam", builder.build());
                      }
        
        );
        
      }
      model.addAttribute("project", project);
      model.addAttribute("modelId", modelId);
      model.addAttribute("action", " ");
      model.addAttribute("initial", "");
      log.debug("MakeApimodelId={}", modelId);
      
    });
        
    return "make-api";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoMakeApiParam {
    String projectId;
    String modelId;
    
    String apiName;
    String apiDescription;
    
    List<MakeApiInputFormat> inputFormats;
    List<MakeApiOutputFormat> outputFormats;
    
    String inputFeatureNameList;
    String inputUserDefineFeatureNameList;
    String inputTypeList;
    String inputDescriptionList;
    
    String outputNameList;
    String outputUserDefineOutputName;
    String outputDescriptionList;
  }
  
  @PostMapping(path = MAKE_API_PATH, params = "action=Save")
  public String doMakeApi(DoMakeApiParam param, Model model, Principal principal) {
    final String userId = principal.getName();
    
    log.debug("{}[action=Save(api)] param={}", MAKE_API_PATH, param);
    
    final PostMakeApiInput makeApiInputParam = PostMakeApiInput.builder()
                                                               .userId(userId)
                                                               .apiName(param.getApiName())
                                                               .apiDescription(param.getApiDescription())
                                                               .inputFields(makeInputFields(param))
                                                               .outputFields(makeOutputFields(param))
                                                               .build();
    final DoMakeApiParam _temp = DoMakeApiParam.builder()
                                               .apiName(param.getApiName())
                                               .apiDescription(param.getApiDescription())
                                               .inputFeatureNameList(param.getInputFeatureNameList())
                                               .inputUserDefineFeatureNameList(
                                                   param.getInputUserDefineFeatureNameList())
                                               .inputTypeList(param.getInputTypeList())
                                               .outputNameList(param.getOutputNameList())
                                               .outputUserDefineOutputName(param.getOutputUserDefineOutputName())
                                               .build();
    
    log.debug("{}[action=Save(api)] makeApiInput={}", MAKE_API_PATH, makeApiInputParam);
    
    makeApiInputCache.put(param.getApiName(), _temp);
    
    log.debug("{}[action=Save(api)] makeApiInputCache={}", MAKE_API_PATH, makeApiInputCache);
    log.debug("{}[action=Save(api)] makeApi_temp={}", MAKE_API_PATH, _temp);
    model.addAttribute("makeApiParam", _temp);
    
    Optional<PostMakeApiOutput> _res = makeApiService.postMakeApi(param.getModelId(), makeApiInputParam);
    log.debug("____res.isPresent={}", _res);
    if (_res.isPresent()) {
      PostMakeApiOutput res = _res.get();
      if ("ok".equalsIgnoreCase(res.getStatus())) {
        model.addAttribute("action", "Save");
        log.debug("action=SaveApi Result={}", res);
        userService.getUserProject(userId, param.getProjectId())
                   .ifPresent(project -> {
                     log.debug("action=SaveApi getUserProject={}", project);
                     model.addAttribute("project", project);
                   });
        // TODO NEW
        if (saveApiToMemory(res, userId)) {
          ApiInfo info = userService.findUserInfoById(userId)
                                    .get()
                                    .getApis()
                                    .get(res.getApiId());
          log.debug("saveApiToMemory_infoTemp = {}", info);
          model.addAttribute("initial", null);
          saveApiToDB(userId, info, model);
        } else {
          log.debug("save Api To Memory failed!");
        }
      }
    }
    
    model.addAttribute("modelId", param.getModelId());
  //TODO
    String pythonServerUrl = inAnalysisConfig.getPythonServerUrl();
    log.debug("PythonServerUrl={}", pythonServerUrl);
    model.addAttribute("pythonServerUrl", pythonServerUrl);

    return "make-api";
    
  }
  
  /**
   * 
   * @param res
   * @param userId
   * @return
   */
  public Boolean saveApiToMemory(PostMakeApiOutput res, final String userId) {
    Boolean result = false;
    
    if (userService.findUserInfoById(userId)
                   .isPresent()) {
      UserInfo userInfo = userService.findUserInfoById(userId)
                                     .get();
      userApiService.getUserApi(AnalysisServer.PYTHON, userId)
                    .ifPresent(output -> {
                      log.debug("userApiService.getUserApis->{}", output);
                      userInfo.setApis(output.getApiList()
                                             .stream()
                                             .collect(Collectors.toMap(ApiInfo::getApiId, apiInfo -> apiInfo)));
                      log.debug("afterSetApisFromPythonOutput->userInfo={}", userInfo);
                    });
      result = true;
    } else {
      log.debug("can't find user by id");
    }
    
    return result;
  }
  
  /**
   * 
   * @param user
   * @param apiInfo
   * @return
   */
  // TODO NEW
  public void saveApiToDB(String userId, ApiInfo apiInfo, Model model) {
    userService.addApi(userId, apiInfo);
    final String apiId = apiInfo.getApiId();
    log.debug("afterSaveApiToDb_userInfosApiList ={}", userService.getUserApiList(userId));
    UserInfo userInfo = userService.findUserInfoById(userId)
                                   .get();
    final ApiInfo savedApi = userInfo.getApis()
                                     .get(apiInfo.getApiId());
    log.debug("action=SaveApi getApibyId={}", savedApi);
    
    model.addAttribute("savedApi", savedApi);
    
    /**
     * ----------------------after refresh api to local Memory----------------
     */
    log.debug("action=SaveApi saveApiInput={}", savedApi.getInputFormats()
                                                        .toString());
    model.addAttribute("savedApiInput", savedApi.getInputFormats()
                                                .toString());
    
    StringBuilder jsonInputString = new StringBuilder();
    StringBuilder jsonOutputString = new StringBuilder();
    List<String> inputToJson = new ArrayList<>();
    inputToJson.add("{");
    for (MakeApiInputFormat inputFormat : savedApi.getInputFormats()) {
      // TODO
      if (inputFormat.equals(savedApi.getInputFormats()
                                     .get(savedApi.getInputFormats()
                                                  .size()
                                         - 1))) {
        log.debug("lastInputFormats={}", inputFormat.getFeatureName());
        JsonFormatUtil.lastInputToJson(inputFormat, inputToJson);
        inputToJson.add("}");
        break;
      }
      JsonFormatUtil.apiInputToJson(inputFormat, inputToJson);
    }
    
    for (String inputJson : inputToJson) {
      log.debug("inputJson={}", inputJson);
      jsonInputString.append(inputJson);
      jsonInputString.append("\n");
    }
    
    model.addAttribute("inputToJson", jsonInputString);
    userInfo.getApis()
            .get(apiInfo.getApiId())
            .setInputJson(jsonInputString);
    
    log.debug("action=SaveApi saveApiOutputFormat={}", savedApi.getOutputFormats()
                                                               .toString());
    model.addAttribute("savedApiOutput", savedApi.getOutputFormats()
                                                 .toString());
    
    List<String> outputToJson = new ArrayList<>();
    outputToJson.add("{");
    for (MakeApiOutputFormat outputFormat : savedApi.getOutputFormats()) {
      // TODO
      if (outputFormat.equals(savedApi.getOutputFormats()
                                      .get(savedApi.getOutputFormats()
                                                   .size()
                                          - 1))) {
        log.debug("lastOutputFormats={}", outputFormat.getOutputName());
        JsonFormatUtil.lastOutputToJson(outputFormat, outputToJson);
        outputToJson.add("}");
        break;
      }
      JsonFormatUtil.apiOutputToJson(outputFormat, outputToJson);
    }
    
    for (String outputJson : outputToJson) {
      log.debug("outputJson={}", outputJson);
      jsonOutputString.append(outputJson);
      jsonOutputString.append("\n");
    }
    
    model.addAttribute("outputToJson", jsonOutputString);
    userInfo.getApis()
            .get(apiId)
            .setOutputJson(jsonOutputString);
    
  }
  
  private List<MakeApiInputFormat> makeInputFields(DoMakeApiParam param) {
    List<MakeApiInputFormat> result = new ArrayList<>();
    
    String[] inputFeatureNameArray = param.getInputFeatureNameList()
                                          .split(",");
    String[] inputUserDefineFeatureNameArray = param.getInputUserDefineFeatureNameList()
                                                    .split(",");
    String[] inputTypeArray = param.getInputTypeList()
                                   .split(",");
    String[] inputDescriptionArray = param.getInputDescriptionList()
                                          .split(",");
    showApi.setFeatureNum(inputUserDefineFeatureNameArray.length);
    
    if (ArrayUtils.isSameLength(inputFeatureNameArray, inputUserDefineFeatureNameArray)
        && ArrayUtils.isSameLength(inputFeatureNameArray, inputTypeArray)
        && ArrayUtils.isSameLength(inputFeatureNameArray, inputDescriptionArray)) {
      for (int i = 0; i < inputFeatureNameArray.length; i++) {
        result.add(MakeApiInputFormat.builder()
                                     .featureName(inputFeatureNameArray[i])
                                     .userDefineFeatureName(inputUserDefineFeatureNameArray[i])
                                     .type(inputTypeArray[i])
                                     .description(inputDescriptionArray[i])
                                     .build());
        
      }
    }
    log.debug("makeInputFields={}", result);
    return result;
  }
  
  private List<MakeApiOutputFormat> makeOutputFields(DoMakeApiParam param) {
    List<MakeApiOutputFormat> result = new ArrayList<>();
    String[] outputNameArray = param.getOutputNameList()
                                    .split(",");
    
    log.debug("outputNameArray={}", "" + outputNameArray);
    
    log.debug("param.getOutputUserDefineOutputName={}", "" + param.getOutputUserDefineOutputName());
    String[] outputUserDefineOutputNameArray = param.getOutputUserDefineOutputName()
                                                    .split(",");
    log.debug("outputUserDefineOutputNameList={}", "" + outputUserDefineOutputNameArray);
    
    log.debug("param.getOutputDescriptionList()={}", "" + param.getOutputDescriptionList());
    String[] outputDescriptionArray = param.getOutputDescriptionList()
                                           .split(",");
    
    log.debug("outputDescriptionArray={}", "" + outputDescriptionArray);
    
    showApi.setOutputNum(outputNameArray.length);
    
    if (ArrayUtils.isSameLength(outputNameArray, outputDescriptionArray)) {
      for (int i = 0; i < outputNameArray.length; i++) {
        result.add(MakeApiOutputFormat.builder()
                                      .outputName(outputNameArray[i])
                                      .userDefineOutpuName(outputUserDefineOutputNameArray[i])
                                      .description(outputDescriptionArray[i])
                                      .build());
      }
    }
    log.debug("makeOutputFields={}", result);
    return result;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ApiPreviewParam {
    String apiName;
    String apiPath;
    Integer featureNum;
    Integer outputNum;
    List<String> featureName;
    List<String> userDefineFeatureName;
    List<String> inputType;
    List<String> output;
    List<String> userDefineOutputName;
    List<String> outputType;
  }
  
  @PostMapping(path = MAKE_API_PATH, params = "action=Cancel")
  public String cancel(String projectId, String modelId, Model model) {
    log.debug("{}[action=Cancel]", MAKE_API_PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId + ModelManagementController.MODEL_PATH;
    
  }
  
  @PostMapping(path = MAKE_API_PATH, params = "action=Back To Project")
  public String backToProject(String projectId, Model model) {
    log.debug("{}[action = Back To Project]", MAKE_API_PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
}
