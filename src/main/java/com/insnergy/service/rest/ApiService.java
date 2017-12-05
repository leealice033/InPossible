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
import com.insnergy.repo.ApiEntityRepo;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.AnalysisServerUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiService {
  
  private static final String API_PATH = "/api";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public ApiService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate, ApiEntityRepo apiRepo) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  /**
   * 取得單一API資訊(by ID)
   * 
   * @author Alice
   *
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetApiOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<GetApiListOutput> apiList;
  }
  
  // TODO change to public output
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetApiListOutput {
    @NonNull
    String userId;
    
    @NonNull
    String modelId;
    
    @NonNull
    String apiId;
    
    @NonNull
    String apiName;
    
    @NonNull
    String apiDescription;
    
    @NonNull
    String apiPath;
    
    @NonNull
    List<GetUserApiInputFormatPublic> inputFormat;
    
    @NonNull
    List<GetUserApiOutputFormatPublic> outputFormat;
    
    Integer usageAmount;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetUserApiInputFormatPublic {
    @NonNull
    String featureName;
    
    @NonNull
    String userDefineFeatureName;
    
    @NonNull
    String type;
    
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetUserApiOutputFormatPublic {
    @NonNull
    String outputName;
    
    String userDefineOutputName;
    
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetApiResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<GetApiListResponse> api_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetApiListResponse {
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
  
  // FIXME user_define_output_name
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserApiOutputFormat {
    @NonNull
    String output_name;
    String user_define_output_name;
    String description;
  }
  
  public Optional<GetApiOutput> getApiById(@NonNull AnalysisServer server, @NonNull String apiId) {
    GetApiOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, apiId);
      log.debug("GetApi serverurl ={}", serverUrl);
      
      final ResponseEntity<GetApiResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetApiResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetApiResponse res = resEntity.getBody();
        final List<GetApiListOutput> apiListForOutput = res.getApi_list()
                                                           .stream()
                                                           .map(this::toApiListOutput)
                                                           .collect(Collectors.toList());
        
        result = GetApiOutput.builder()
                             .status(res.getStatus())
                             .description(res.getDescription())
                             .apiList(apiListForOutput)
                             .build();
        
        log.debug("getApiById result={}", result);
      }
      
    } catch (Exception e) {
      log.error("getApi error: {}", ExceptionUtils.getMessage(e), e);
      
    }
    return Optional.ofNullable(result);
  }
  
  private GetApiListOutput toApiListOutput(GetApiListResponse apiList) {
    return GetApiListOutput.builder()
                           .userId(apiList.getUser_id())
                           .modelId(apiList.model_id)
                           .apiId(apiList.api_id)
                           .apiName(apiList.api_name)
                           .apiDescription(apiList.api_description)
                           .apiPath(apiList.getApi_path())
                           .inputFormat(apiList.getInput_format()
                                               .stream()
                                               .map(this::toGetUserApiInputFormat)
                                               .collect(Collectors.toList()))
                           .outputFormat(apiList.getOutput_format()
                                                .stream()
                                                .map(this::toGetUserApiOutputFormat)
                                                .collect(Collectors.toList()))
                           .usageAmount(apiList.usage_amount)
                           .build();
  }
  
  private GetUserApiInputFormatPublic toGetUserApiInputFormat(GetUserApiInputFormat inputFormatRes) {
    return GetUserApiInputFormatPublic.builder()
                                      .featureName(inputFormatRes.getFeature_name())
                                      .userDefineFeatureName(inputFormatRes.getUser_define_feature_name())
                                      .type(inputFormatRes.getType())
                                      .description(inputFormatRes.getDescription())
                                      .build();
  }
  
  private GetUserApiOutputFormatPublic toGetUserApiOutputFormat(GetUserApiOutputFormat outputFormatRes) {
    return GetUserApiOutputFormatPublic.builder()
                                       .outputName(outputFormatRes.getOutput_name())
                                       .userDefineOutputName(outputFormatRes.getUser_define_output_name())
                                       .description(outputFormatRes.getDescription())
                                       .build();
  }
  
  /**
   * <CURD> DELETE API
   * 
   * @author Alice
   * 
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DeleteApiResponse {
    String status;
    String description;
  }
  
  public Optional<DeleteApiResponse> deleteApi(@NonNull AnalysisServer server, @NonNull String apiId) {
    DeleteApiResponse result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, apiId);
      log.debug("DeleteApiUrl={}", serverUrl);
      
      final ResponseEntity<DeleteApiResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.DELETE,
          HttpEntity.EMPTY, DeleteApiResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        result = resEntity.getBody();
      }
      
    } catch (Exception e) {
      log.error("DeleteApi error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  /**
   * <CURD> UPDATE API
   * 
   * @author Alice
   * 
   */
  // TODO EDIT API
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutUpdateApiInput {
    String userId;
    String apiId;
    String apiName;
    String apiDescription;
    List<PutUpdateApiInputFormat> inputFormat;
    List<PutUpdateApiOutputFormat> outputFormat;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutUpdateApiInputFormat {
    @NonNull
    String featureName;
    
    @NonNull
    String userDefineFeatureName;
    
    @NonNull
    String type;
    
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutUpdateApiOutputFormat {
    @NonNull
    String outputName;
    
    @NonNull
    String userDefineOutputName;
    
    @NonNull
    String description;
  }
  
  // FIXME
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutUpdateApiOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String modelId;
    
    @NonNull
    String apiId;
    
    @NonNull
    String apiName;
    
    @NonNull
    String apiPath;
    
    String apiDescription;
    
    @NonNull
    List<PutUpdateApiInputFormat> inputFormats;
    
    @NonNull
    List<PutUpdateApiOutputFormat> outputFormats;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutUpdateApiRequest {
    String user_id;
    
    @NonNull
    String api_name;
    
    @NonNull
    String api_description;
    
    List<PutUpdateApiInputFormatRequest> input_format;
    
    List<PutUpdateApiOutputFormatRequest> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutUpdateApiInputFormatRequest {
    String feature_name;
    String user_define_feature_name;
    String type;
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutUpdateApiOutputFormatRequest {
    String output_name;
    String user_define_output_name;
    String description;
  }
  
  // PUT RESPONSE
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutUpdateApiResponse {
    
    @NonNull
    String status;
    
    @NonNull
    String description;
    
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
    List<PutUpdateApiInputFormatResponse> input_format;
    
    @NonNull
    List<PutUpdateApiOutputFormatResponse> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutUpdateApiInputFormatResponse {
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
  private static class PutUpdateApiOutputFormatResponse {
    
    @NonNull
    String output_name;
    
    @NonNull
    String user_define_output_name;
    
    String description;
  }
  
  public Optional<PutUpdateApiOutput> updateApi(@NonNull AnalysisServer server, @NonNull PutUpdateApiInput input) {
    PutUpdateApiOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, input.getApiId());
      log.debug("PutUpdateApi_url={},getApiId={}", serverUrl, input.getApiId());
      
      final List<PutUpdateApiInputFormatRequest> inputFormat = input.getInputFormat()
                                                                    .stream()
                                                                    .map(inputFormatForInput -> {
                                                                      return PutUpdateApiInputFormatRequest.builder()
                                                                                                           .feature_name(
                                                                                                               inputFormatForInput.getFeatureName())
                                                                                                           .user_define_feature_name(
                                                                                                               inputFormatForInput.getUserDefineFeatureName())
                                                                                                           .type(
                                                                                                               inputFormatForInput.getType())
                                                                                                           .description(
                                                                                                               inputFormatForInput.getDescription())
                                                                                                           .build();
                                                                    })
                                                                    .collect(Collectors.toList());
      
      final List<PutUpdateApiOutputFormatRequest> outputFormat = input.getOutputFormat()
                                                                      .stream()
                                                                      .map(outputFormatForInput -> {
                                                                        return PutUpdateApiOutputFormatRequest.builder()
                                                                                                              .output_name(
                                                                                                                  outputFormatForInput.getOutputName())
                                                                                                              .user_define_output_name(
                                                                                                                  outputFormatForInput.getUserDefineOutputName())
                                                                                                              .description(
                                                                                                                  outputFormatForInput.getDescription())
                                                                                                              .build();
                                                                      })
                                                                      .collect(Collectors.toList());
      // FIXME
      final PutUpdateApiRequest req = PutUpdateApiRequest.builder()
                                                         .user_id(input.getApiId())
                                                         .api_name(input.getApiName())
                                                         .api_description(input.getApiDescription())
                                                         .input_format(inputFormat)
                                                         .output_format(outputFormat)
                                                         .build();
      
      final HttpEntity<PutUpdateApiRequest> reqEntity = new HttpEntity<PutUpdateApiRequest>(req);
      final ResponseEntity<PutUpdateApiResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.PUT, reqEntity,
          PutUpdateApiResponse.class);
      log.debug("resEntity.getStatusCode()={}, resEntity.hasBody()={}", resEntity.getStatusCode(), resEntity.hasBody());
      
      // TODO
      if (HttpStatus.CREATED.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        PutUpdateApiResponse res = resEntity.getBody();
        
        result = PutUpdateApiOutput.builder()
                                   .status(res.getStatus())
                                   .description(res.getDescription())
                                   .modelId(res.getModel_id())
                                   .apiId(res.getApi_id())
                                   .apiName(res.getApi_name())
                                   .apiPath(res.getApi_path())
                                   .apiDescription(res.getApi_description())
                                   .inputFormats(res.getInput_format()
                                                    .stream()
                                                    .map(inputFormatOutput -> {
                                                      return PutUpdateApiInputFormat.builder()
                                                                                    .featureName(
                                                                                        inputFormatOutput.getFeature_name())
                                                                                    .userDefineFeatureName(
                                                                                        inputFormatOutput.getUser_define_feature_name())
                                                                                    .type(inputFormatOutput.getType())
                                                                                    .description(
                                                                                        inputFormatOutput.getDescription())
                                                                                    .build();
                                                    })
                                                    .collect(Collectors.toList()))
                                   
                                   .outputFormats(res.getOutput_format()
                                                     .stream()
                                                     .map(outputFormatOutput -> {
                                                       return PutUpdateApiOutputFormat.builder()
                                                                                      .outputName(
                                                                                          outputFormatOutput.getOutput_name())
                                                                                      .userDefineOutputName(
                                                                                          outputFormatOutput.getUser_define_output_name())
                                                                                      .description(
                                                                                          outputFormatOutput.getDescription())
                                                                                      .build();
                                                     })
                                                     .collect(Collectors.toList()))
                                   .build();
        
        log.debug("UpdateApi_resultForOutput={}", result);
      }
    } catch (
    
    Exception e) {
      log.error("PutUpdateApi error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
    
  }
  
}
