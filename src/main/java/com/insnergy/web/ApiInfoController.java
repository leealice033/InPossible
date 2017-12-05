package com.insnergy.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
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

public class ApiInfoController {
  private static final String API_INFO_PATH = "/api-info";
  private final UserInfoService userService;
  private final InAnalysisConfig inAnalysisConfig;
  
  public ApiInfoController(UserInfoService userService, InAnalysisConfig inAnalysisConfig) {
    this.userService = userService;
    this.inAnalysisConfig = inAnalysisConfig;
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
    StringBuilder inputJson;
    StringBuilder outputJson;
    List<String> featureName;
    List<String> userDefineFeatureName;
    List<String> inputType;
    List<String> output;
    List<String> userDefineOutputName;
    List<String> outputType;
  }
  
  @GetMapping(API_INFO_PATH + "/{apiId:.+}")
  public String toApiInfoPage(@PathVariable String apiId, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    
    UserInfo userInfo = userService.findUserInfoById(userId)
                                   .get();
    final ApiInfo apiToShow = userInfo.getApis()
                                      .get(apiId);
    log.debug("action=SaveApi getApibyId={}", apiToShow);
    // input
    model.addAttribute("savedApiInput", apiToShow.getInputFormats()
                                                 .toString());
    
    StringBuilder jsonInputString = buildInputJson(apiToShow);
    StringBuilder jsonOutputString = buildOutputJson(apiToShow);
    log.debug("jsonInputString={}", jsonInputString);
    log.debug("jsonOutputString={}", jsonOutputString);
    ApiPreviewParam _api = ApiPreviewParam.builder()
                                          .apiName(apiToShow.getApiName())
                                          .apiPath(apiToShow.getApiPath())
                                          .inputJson(jsonInputString)
                                          .outputJson(jsonOutputString)
                                          .build();
    model.addAttribute("api", _api);
    String pythonServerUrl = inAnalysisConfig.getPythonServerUrl();
    log.debug("PythonServerUrl={}", pythonServerUrl);
    model.addAttribute("pythonServerUrl", pythonServerUrl);
    
    return "api-info";
  }
  
  // http://127.0.0.1:8008/api-management
  
  @PostMapping(path = API_INFO_PATH, params = "action=Back")
  public String cancel(String apiId, Model model, String back, RedirectAttributes redirectAttributes) {
    log.debug("{}[action=Back={}]", API_INFO_PATH, back);
    if (StringUtils.equals("api-management", back)) {
      return "redirect:" + "/api-management";
    } else if (StringUtils.equals("user-management", back)) {
      return "redirect:" + "/user-management";
    }
    return "redirect:" + "/api-management";
  }
  
  public StringBuilder buildInputJson(ApiInfo api) {
    StringBuilder jsonInputString = new StringBuilder();
    List<String> inputToJson = new ArrayList<>();
    inputToJson.add("{");
    for (MakeApiInputFormat inputFormat : api.getInputFormats()) {
      if (inputFormat.equals(api.getInputFormats()
                                .get(api.getInputFormats()
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
    
    api.setInputJson(jsonInputString);
    
    log.debug("action=SaveApi saveApiOutputFormat={}", api.getOutputFormats()
                                                          .toString());
    StringBuilder result = jsonInputString;
    log.debug("buildInputJson_result = {}", result);
    return result;
  }
  
  public StringBuilder buildOutputJson(ApiInfo api) {
    StringBuilder jsonOutputString = new StringBuilder();
    log.debug("action=SaveApi saveApiOutputFormat={}", api.getOutputFormats()
                                                          .toString());
    List<String> outputToJson = new ArrayList<>();
    outputToJson.add("{");
    for (MakeApiOutputFormat outputFormat : api.getOutputFormats()) {
      if (outputFormat.equals(api.getOutputFormats()
                                 .get(api.getOutputFormats()
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
    
    api.setOutputJson(jsonOutputString);
    StringBuilder result = jsonOutputString;
    log.debug("buildOutputJson_result = {}", result);
    return result;
  }
}
