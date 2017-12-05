package com.insnergy.service.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.AnalysisServerUtil;
import com.insnergy.vo.ApiInfo;
import com.insnergy.web.ApiManagementController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserApiService {
  
  private static final String USER_PATH = "/user";
  private static final String API_PATH = "/api";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public UserApiService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  /**
   * 以User取得API資訊(by UserID)
   * 
   * @author Alice
   *
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetUserApiOutput {
    String status;
    String description;
    List<ApiInfo> apiList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserApiResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<GetUserApiIdList> api_list;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserApiIdList {
    @NonNull
    String user_id;
    
    @NonNull
    String model_id;
    
    @NonNull
    String api_id;
    
    @NonNull
    String api_name;
    
    @NonNull
    String api_description;
    
    @NonNull
    String api_path;
    
    @NonNull
    List<GetUserApiInputFormat> input_format;
    
    @NonNull
    List<GetUserApiOutputFormat> output_format;
    
    Integer usage_amount;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserApiInputFormat {
    @NonNull
    String feature_name;
    
    @NonNull
    String user_define_feature_name;
    
    @NonNull
    String type;
    
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserApiOutputFormat {
    @NonNull
    String output_name;
    
    String user_define_output_name;
    
    @NonNull
    String description;
  }
  
  public Optional<GetUserApiOutput> getUserApi(@NonNull AnalysisServer server, @NonNull String userId) {
    GetUserApiOutput result = null;
    try {
      String url = AnalysisServerUtil.buildUrl(config, server, USER_PATH, userId, API_PATH);
      log.debug("url={}", url);
      final ResponseEntity<GetUserApiResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
          GetUserApiResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        GetUserApiResponse res = resEntity.getBody();
        
        final List<ApiInfo> apiList = res.getApi_list()
                                         .stream()
                                         .map(api -> buildApiInfo(config, server, api))
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .collect(Collectors.toList());
        
        result = GetUserApiOutput.builder()
                                 .status(res.getStatus())
                                 .description(res.getDescription())
                                 .apiList(apiList)
                                 .build();
      }
      
    } catch (Exception e) {
      log.error("getUserApi error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.debug("getUserApi result={}", result);
    return Optional.ofNullable(result);
  }
  
  Optional<ApiInfo> buildApiInfo(@NonNull InAnalysisConfig config, @NonNull AnalysisServer server,
      @NonNull final GetUserApiIdList api) {
    ApiInfo result = null;
    try {
      final List<MakeApiInputFormat> inputFormats = api.getInput_format()
                                                       .stream()
                                                       .map(input -> {
                                                         return MakeApiInputFormat.builder()
                                                                                  .featureName(input.getFeature_name())
                                                                                  .userDefineFeatureName(
                                                                                      input.getUser_define_feature_name())
                                                                                  .type(input.getType())
                                                                                  .description(input.getDescription())
                                                                                  .build();
                                                       })
                                                       .collect(Collectors.toList());
      log.debug("getUserApi inputFormats={}", inputFormats);
      
      final List<MakeApiOutputFormat> outputFormats = api.getOutput_format()
                                                         .stream()
                                                         .map(output -> {
                                                           return MakeApiOutputFormat.builder()
                                                                                     .outputName(
                                                                                         output.getOutput_name())
                                                                                     .userDefineOutpuName(
                                                                                         output.getUser_define_output_name())
                                                                                     .description(
                                                                                         output.getDescription())
                                                                                     .build();
                                                         })
                                                         .collect(Collectors.toList());
      
      log.debug("getUserApi outputFormats={}", outputFormats);
      
      result = ApiInfo.builder()
                      .server(server)
                      .userId(api.getUser_id())
                      .modelId(api.getModel_id())
                      .apiId(api.getApi_id())
                      .apiName(api.getApi_name())
                      .apiPath(api.getApi_path())
                      .deleteUrl(ApiManagementController.getDeleteApiPath(api.getApi_id()))
                      .apiDescription(api.getApi_description())
                      .inputFormats(inputFormats)
                      .outputFormats(outputFormats)
                      .usageAmount(api.getUsage_amount())
                      .build();
      
    } catch (Exception e) {
      log.error("buildApiInfo error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
