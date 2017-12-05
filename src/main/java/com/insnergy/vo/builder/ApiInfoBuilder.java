package com.insnergy.vo.builder;

import java.util.List;
import java.util.stream.Collectors;

import com.insnergy.domain.ApiEntity;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ApiService;
import com.insnergy.service.rest.ApiService.GetApiListOutput;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ApiInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ApiInfoBuilder {
  private UserInfoService userService;
  private ApiService apiService;
  
  /*
   * APIINFO Long index;
   * 
   * AnalysisServer server; String userId; String projectId; String modelId;
   * String apiId; String apiName; String apiDescription; String apiPath; String
   * deleteUrl; int usageAmount;
   * 
   * List<MakeApiInputFormat> inputFormats; List<MakeApiOutputFormat>
   * outputFormats; StringBuilder inputJson; StringBuilder outputJson;
   */
  
  public static ApiInfo build(ApiEntity entity) {
    log.debug("input={}", entity);
    if (apiService.getApiById(AnalysisServer.PYTHON, entity.getId())
                  .isPresent()) {
      log.debug("apiServiceGetApi={}", apiService.getApiById(AnalysisServer.PYTHON, entity.getId())
                                                 .get());
      GetApiListOutput apiOutput = apiService.getApiById(AnalysisServer.PYTHON, entity.getId())
                                             .get()
                                             .getApiList()
                                             .get(0);
      ApiInfo apiTemp = ApiInfo.builder()
                               .index(entity.getApiIndex())
                               .server(AnalysisServer.PYTHON)
                               .userId(apiOutput.getUserId())
                               .modelId(apiOutput.getModelId())
                               .apiId(entity.getId())
                               .apiName(entity.getName())
                               .apiDescription(apiOutput.getApiDescription())
                               .apiPath(entity.getPath())
                               // .deleteUrl("dddd")
                               .usageAmount(apiOutput.getUsageAmount())
                               .inputFormats(apiOutput.getInputFormat()
                                                      .stream()
                                                      .map(inputFormat -> {
                                                        return MakeApiInputFormat.builder()
                                                                                 .featureName(
                                                                                     inputFormat.getFeatureName())
                                                                                 .userDefineFeatureName(
                                                                                     inputFormat.getUserDefineFeatureName())
                                                                                 .description(
                                                                                     inputFormat.getDescription())
                                                                                 .type(inputFormat.getType())
                                                                                 .build();
                                                      })
                                                      .collect(Collectors.toList()))
                               .outputFormats(apiOutput.getOutputFormat()
                                                       .stream()
                                                       .map(outputFormat -> {
                                                         return MakeApiOutputFormat.builder()
                                                                                   .outputName(
                                                                                       outputFormat.getOutputName())
                                                                                   .userDefineOutpuName(
                                                                                       outputFormat.getUserDefineOutputName())
                                                                                   .description(
                                                                                       outputFormat.getDescription())
                                                                                   .build();
                                                       })
                                                       .collect(Collectors.toList()))
                               // .inputJson(inputJson)
                               // .outputJson(outputJson)
                               .build();
      
      log.debug("apiTempByBuild={}", apiTemp);
    }
    return ApiInfo.builder()
                  .index(entity.getApiIndex())
                  .apiId(entity.getId())
                  .apiName(entity.getName())
                  .apiDescription(entity.getDescription())
                  .apiPath(entity.getPath())
                  .build();
  }
  
}
