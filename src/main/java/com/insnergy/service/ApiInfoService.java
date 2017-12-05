package com.insnergy.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.insnergy.repo.ApiEntityRepo;
import com.insnergy.service.rest.ApiService;
import com.insnergy.service.rest.ApiService.GetApiListOutput;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ApiInfo;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

//TODO

@Service
@Slf4j
public class ApiInfoService {
  
  private final UserInfoService userService;
  private final ApiService apiServiceRest;// get & delete
  private final ApiEntityRepo apiRepo;
  
  public ApiInfoService(UserInfoService userService, ApiService apiServiceRest, ApiEntityRepo apiRepo) {
    this.userService = userService;
    this.apiServiceRest = apiServiceRest;
    this.apiRepo = apiRepo;
  }
  
  public Optional<ApiInfo> buildApiInfoToDB(@NonNull AnalysisServer server, @NonNull ApiInfo api) {
    log.debug("[buildApiInfoToDB]server={}, api={}", server, api);
    ApiInfo result = null;
    String apiId = api.getApiId();
    try {
      
      if (apiServiceRest.getApiById(server, apiId)
                        .isPresent()) {
        final GetApiListOutput apiTemp = apiServiceRest.getApiById(server, apiId)
                                                       .get()
                                                       .getApiList()
                                                       .get(0);
        
        log.debug("beginToBuild_byApiRestId={}", apiTemp.getApiId());
        result = ApiInfo.builder()
                        .index(api.getIndex())
                        .apiId(apiTemp.getApiId())
                        .apiName(apiTemp.getApiName())
                        .apiDescription(apiTemp.getApiDescription())
                        .apiPath(apiTemp.getApiPath())
                        .inputFormats(apiTemp.getInputFormat()
                                             .stream()
                                             .map(inputFormat -> {
                                               return MakeApiInputFormat.builder()
                                                                        .featureName(inputFormat.getFeatureName())
                                                                        .userDefineFeatureName(
                                                                            inputFormat.getUserDefineFeatureName())
                                                                        .type(inputFormat.getType())
                                                                        .description(inputFormat.getDescription())
                                                                        .build();
                                             })
                                             .collect(Collectors.toList()))
                        .outputFormats(apiTemp.getOutputFormat()
                                              .stream()
                                              .map(outputFormat -> {
                                                return MakeApiOutputFormat.builder()
                                                                          .outputName(outputFormat.getOutputName())
                                                                          .userDefineOutpuName(
                                                                              outputFormat.getUserDefineOutputName())
                                                                          .description(outputFormat.getDescription())
                                                                          .build();
                                              })
                                              .collect(Collectors.toList()))
                        .build();
        
      }
    } catch (Exception e) {
      log.error("error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  public void addApiToDB(@NonNull String userId, @NonNull ApiInfo apiInfo) {
    userService.addApi(userId, apiInfo);
    log.debug("After_DB_CREATE_API_return void");
  }
  
}
