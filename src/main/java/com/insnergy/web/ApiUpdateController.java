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

import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ApiService;
import com.insnergy.service.rest.UserApiService;
import com.insnergy.service.rest.ApiService.GetUserApiInputFormatPublic;
import com.insnergy.service.rest.ApiService.GetUserApiOutputFormatPublic;
import com.insnergy.service.rest.ApiService.PutUpdateApiInput;
import com.insnergy.service.rest.ApiService.PutUpdateApiInputFormat;
import com.insnergy.service.rest.ApiService.PutUpdateApiOutput;
import com.insnergy.service.rest.ApiService.PutUpdateApiOutputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.JsonFormatUtil;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.UserInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ApiUpdateController {
  private static final String API_PATH = "/api-management";
  private static final String EDIT_API = "/update-api";
  
  private final UserInfoService userService;
  private final UserApiService userApiService;
  private final ApiService apiService;
  private final Map<String, DoUpdateApiParam> editApiInputCache;
  
  public ApiUpdateController(UserInfoService userService, UserApiService userApiService, ApiService apiService) {
    this.userService = userService;
    this.userApiService = userApiService;
    this.apiService = apiService;
    this.editApiInputCache = new ConcurrentHashMap<>();
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DoUpdateApiParam {
    String apiId;
    String apiName;
    String apiDescription;
    String userId;
    List<GetUserApiInputFormatPublic> inputFormats;
    List<GetUserApiOutputFormatPublic> outputFormats;
    
    String inputFeatureNameList;
    String inputUserDefineFeatureNameList;
    String inputTypeList;
    String inputDescriptionList;
    
    String outputNameList;
    String outputUserDefineOutputName;
    String outputDescriptionList;
  }
  
  public String refreshPage(Principal principal, Model model) {
    final String userId = principal.getName();
    log.debug("index userId={}", userId);
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      
      userApiService.getUserApi(AnalysisServer.PYTHON, userId)
                    .ifPresent(output -> {
                      userInfo.setApis(output.getApiList()
                                             .stream()
                                             .collect(Collectors.toMap(ApiInfo::getApiId, apiInfo -> apiInfo)));
                    });
                    
      model.addAttribute("userId", userId);
      model.addAttribute("apis", userInfo.getApis()
                                         .values()
                                         .stream()
                                         .collect(Collectors.toList()));
      
    });
    return "api-management";
  }
  
  // TODO
  @GetMapping(EDIT_API + "/{apiId:.+}")
  public String toUpdateApiPage(@PathVariable String apiId, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    
    apiService.getApiById(AnalysisServer.PYTHON, apiId)
              .ifPresent(api -> {
                
                final DoUpdateApiParam.DoUpdateApiParamBuilder builder = DoUpdateApiParam.builder()
                                                                                         .apiId(apiId)
                                                                                         .userId(userId)
                                                                                         .apiName(api.getApiList()
                                                                                                     .get(0)
                                                                                                     .getApiName())
                                                                                         .apiDescription(
                                                                                             api.getApiList()
                                                                                                .get(0)
                                                                                                .getApiDescription());
                builder.inputFormats(api.getApiList()
                                        .get(0)
                                        .getInputFormat()
                                        .stream()
                                        .map(inputFormat -> {
                                          return GetUserApiInputFormatPublic.builder()
                                                                            .featureName(inputFormat.getFeatureName())
                                                                            .userDefineFeatureName(
                                                                                inputFormat.getUserDefineFeatureName())
                                                                            .type(inputFormat.getType())
                                                                            .description(inputFormat.getFeatureName())
                                                                            .build();
                                        })
                                        .collect(Collectors.toList()));
                                        
                builder.outputFormats(api.getApiList()
                                         .get(0)
                                         .getOutputFormat()
                                         .stream()
                                         .map(outputFormat -> {
                                           return GetUserApiOutputFormatPublic.builder()
                                                                              .outputName(outputFormat.getOutputName())
                                                                              .userDefineOutputName(
                                                                                  outputFormat.getUserDefineOutputName())
                                                                              .description(
                                                                                  outputFormat.getDescription())
                                                                              .build();
                                         })
                                         .collect(Collectors.toList()));
                                         
                log.debug("DoUpdateApiParam.DoUpdateApiParamBuilder_builder={}", builder.build());
                model.addAttribute("updateApiParam", builder.build());
              });
    model.addAttribute("action", " ");
    return "update-api-page";
    
  }
  
  // TODO
  @PostMapping(path = EDIT_API, params = "action=Cancel")
  public String cancel(String modelId, Model model) {
    log.debug("{}[action=Cancel]", EDIT_API);
    return "redirect:" + "/api-management";
    
  }
  
  // TODO
  @PostMapping(path = EDIT_API, params = "action=SaveChange")
  public String saveApiAfterEdit(DoUpdateApiParam param, Model model, Principal principal) {
    String userId = principal.getName();
    log.debug("{}[action=Save(edited_api)] param={}", API_PATH, param);
    
    final PutUpdateApiInput editParams = PutUpdateApiInput.builder()
                                                          .userId(userId)
                                                          .apiId(param.getApiId())
                                                          .apiName(param.getApiName())
                                                          .apiDescription(param.getApiDescription())
                                                          .inputFormat(makeEditInputFormat(param))
                                                          .outputFormat(makeEditOutputFormat(param))
                                                          .build();
    
    final DoUpdateApiParam _temp = DoUpdateApiParam.builder()
                                                   .userId(userId)
                                                   .apiName(param.getApiName())
                                                   .apiId(param.getApiId())
                                                   .apiDescription(param.getApiDescription())
                                                   .inputFeatureNameList(param.getInputFeatureNameList())
                                                   .inputUserDefineFeatureNameList(
                                                       param.getInputUserDefineFeatureNameList())
                                                   .inputTypeList(param.getInputTypeList())
                                                   .outputNameList(param.getOutputNameList())
                                                   .build();
    
    log.debug("testest_inputFeatureNameList={}", param.getInputFeatureNameList());
    log.debug("testest_inputUserFeatureName={}", param.getInputUserDefineFeatureNameList());
    log.debug("testest_getInputTypeList={}", param.getInputTypeList());
    log.debug("testest_getoutputNameList={}", param.getOutputNameList());
    
    log.debug("{}[action=SaveAfterEdit(api)] UpdateApiInput={}", API_PATH, editParams);
    editApiInputCache.put(param.getApiName(), _temp);
    
    log.debug("{}[action=Save(EDIT_API)] editApiInputCache={}", API_PATH, editApiInputCache);
    log.debug("{}[action=Save(EDIT_API)] makeApi_temp={}", API_PATH, _temp);
    model.addAttribute("updateApiParam", _temp);
    // model.addAttribute("editParams", editParams);
    
    // TODO call python Api
    Optional<PutUpdateApiOutput> _res = apiService.updateApi(AnalysisServer.PYTHON, editParams);
    log.debug("PutUpdateApiOutput_res.isPresent={}", _res);
    
    if (_res.isPresent()) {
      PutUpdateApiOutput res = _res.get();
      log.debug("_res.isPresent={}", res);
      
      if ("ok".equalsIgnoreCase(res.getStatus())) {
        model.addAttribute("action", "SaveChange");
        
        log.debug("action=PutUpdateApi Result={}", res);
        
        userService.findUserInfoById(userId)
                   .ifPresent(userInfo -> {
                     userApiService.getUserApi(AnalysisServer.PYTHON, userId)
                                   .ifPresent(output -> {
                                     userInfo.setApis(output.getApiList()
                                                            .stream()
                                                            .collect(Collectors.toMap(ApiInfo::getApiId,
                                                                apiInfo -> apiInfo)));
                                     final ApiInfo savedApi = userInfo.getApis()
                                                                      .get(res.getApiId());
                                     log.debug("action=SaveApi getApibyId={}", savedApi);
                                     
                                     model.addAttribute("savedApi", savedApi);
                                     log.debug("action=SaveApi saveApiInput={}", savedApi.getInputFormats()
                                                                                         .toString());
                                     model.addAttribute("savedApiInput", savedApi.getInputFormats()
                                                                                 .toString());
                                     
                                     log.debug("action=SaveApi saveApiOutputFormat={}", savedApi.getOutputFormats()
                                                                                                .toString());
                                     model.addAttribute("savedApiOutput", savedApi.getOutputFormats()
                                                                                  .toString());
                                     
                                     // TODO
                                     StringBuilder jsonInputString = new StringBuilder();
                                     StringBuilder jsonOutputString = new StringBuilder();
                                     List<String> inputToJson = new ArrayList<>();
                                     inputToJson.add("{");
                                     for (MakeApiInputFormat inputFormat : savedApi.getInputFormats()) {
                                       JsonFormatUtil.apiInputToJson(inputFormat, inputToJson);
                                     }
                                     inputToJson.add("}");
                                     
                                     for (String inputJson : inputToJson) {
                                       log.debug("inputJson={}", inputJson);
                                       jsonInputString.append(inputJson);
                                       jsonInputString.append("\n");
                                     }
                                     
                                     userInfo.getApis()
                                             .get(res.getApiId())
                                             .setInputJson(jsonInputString);
                                     log.debug("setInputJson", userInfo.getApis()
                                                                       .get(res.getApiId())
                                                                       .getInputJson());
                                     
                                     // TODO
                                     List<String> outputToJson = new ArrayList<>();
                                     outputToJson.add("{");
                                     for (MakeApiOutputFormat outputFormat : savedApi.getOutputFormats()) {
                                       JsonFormatUtil.apiOutputToJson(outputFormat, outputToJson);
                                     }
                                     outputToJson.add("}");
                                     
                                     for (String outputJson : outputToJson) {
                                       log.debug("outputJson={}", outputJson);
                                       jsonOutputString.append(outputJson);
                                       jsonOutputString.append("\n");
                                     }
                                     userInfo.getApis()
                                             .get(res.getApiId())
                                             .setOutputJson(jsonOutputString);
                                     log.debug("setOutputJson", userInfo.getApis()
                                                                        .get(res.getApiId())
                                                                        .getOutputJson());
                                     
                                   });
                   });
        
      }
    }
    model.addAttribute("userId", userId);
    
    return refreshPage(principal, model);
  }
  
  private List<PutUpdateApiInputFormat> makeEditInputFormat(DoUpdateApiParam param) {
    List<PutUpdateApiInputFormat> result = new ArrayList<>();
    
    String[] inputFeatureNameArray = param.getInputFeatureNameList()
                                          .split(",");
    String[] inputUserDefineFeatureNameArray = param.getInputUserDefineFeatureNameList()
                                                    .split(",");
    String[] inputTypeArray = param.getInputTypeList()
                                   .split(",");
    String[] inputDescriptionArray = param.getInputDescriptionList()
                                          .split(",");
    
    if (ArrayUtils.isSameLength(inputFeatureNameArray, inputUserDefineFeatureNameArray)
        && ArrayUtils.isSameLength(inputFeatureNameArray, inputTypeArray)
        && ArrayUtils.isSameLength(inputFeatureNameArray, inputDescriptionArray)) {
      for (int i = 0; i < inputFeatureNameArray.length; i++) {
        result.add(PutUpdateApiInputFormat.builder()
                                          .featureName(inputFeatureNameArray[i])
                                          .userDefineFeatureName(inputUserDefineFeatureNameArray[i])
                                          .type(inputTypeArray[i])
                                          .description(inputDescriptionArray[i])
                                          .build());
        
      }
    }
    log.debug("PutUpdateApiInputFormat={}", result);
    return result;
  }
  
  private List<PutUpdateApiOutputFormat> makeEditOutputFormat(DoUpdateApiParam param) {
    List<PutUpdateApiOutputFormat> result = new ArrayList<>();
    
    log.debug("param.getOutputNameList()={}", "" + param.getOutputNameList());
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
    
    if (ArrayUtils.isSameLength(outputNameArray, outputDescriptionArray)) {
      for (int i = 0; i < outputNameArray.length; i++) {
        result.add(PutUpdateApiOutputFormat.builder()
                                           .outputName(outputNameArray[i])
                                           .userDefineOutputName(outputUserDefineOutputNameArray[i])
                                           .description(outputDescriptionArray[i])
                                           .build());
      }
    }
    log.debug("PutUpdateApiOutputFormat={}", result);
    log.debug("tsssss={}", result.get(0)
                                 .getOutputName());
    return result;
  }
}
