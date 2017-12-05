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
public class MakeApiService {
  private static final String MAKE_API_PATH = "/make-api";/// make-api/{model_id}(POST/GET)
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public MakeApiService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetMakeApiOutput {
    String status;
    String description;
    String modelId;
    List<InputFormat> inputFormats;
    List<String> outputFormats;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class InputFormat {
    String featureName;
    String type;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetMakeApiResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String model_id;
    
    @NonNull
    List<GetInputFormatResponse> input_format;
    
    @NonNull
    List<String> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetInputFormatResponse {
    @NonNull
    String feature_name;
    
    @NonNull
    String type;
  }
  
  public Optional<GetMakeApiOutput> getMakeApi(@NonNull AnalysisServer server, String modelId) {
    GetMakeApiOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, MAKE_API_PATH, modelId);
      final ResponseEntity<GetMakeApiResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetMakeApiResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        GetMakeApiResponse res = resEntity.getBody();
        
        result = GetMakeApiOutput.builder()
                                 .status(res.getStatus())
                                 .description(res.getDescription())
                                 .inputFormats(res.getInput_format()
                                                  .stream()
                                                  .map(f -> {
                                                    return InputFormat.builder()
                                                                      .featureName(f.getFeature_name())
                                                                      .type(f.getType())
                                                                      .build();
                                                  })
                                                  .collect(Collectors.toList()))
                                 .outputFormats(res.getOutput_format())
                                 .build();
        
        log.debug("getMakeApi url : {}", serverUrl);
        log.debug("getMakeApi result={}", result);
      }
    } catch (Exception e) {
      log.error("getMakeApi error: {}", ExceptionUtils.getMessage(e), e);
      
    }
    log.info("getMakeApi[modelId={}] result={}", modelId, result);
    return Optional.ofNullable(result);
  }
  
  // TODO
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostMakeApiInput {
    String userId;
    String apiName;
    String apiDescription;
    List<MakeApiInputFormat> inputFields;
    List<MakeApiOutputFormat> outputFields;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostMakeApiOutput {
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
    List<MakeApiInputFormat> inputFormats;
    
    @NonNull
    List<MakeApiOutputFormat> outputFormats;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class MakeApiInputFormat {
    String featureName;
    String userDefineFeatureName;
    String type;
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class MakeApiOutputFormat {
    String outputName;
    String description;
    String userDefineOutpuName;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostMakeApiRequest {
    @NonNull
    String user_id;
    
    @NonNull
    String api_name;
    
    @NonNull
    String api_description;
    
    List<PostMakeApiInputFormat> input_field;
    
    List<PostMakeApiOutputFormat> output_field;
  }
  
  // POST RESPONSE
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostMakeApiResponse {
    
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
    String api_path;// "api-service/{api_id}"
    
    @NonNull
    List<PostMakeApiInputFormat> input_format;
    
    @NonNull
    List<PostMakeApiOutputFormat> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostMakeApiInputFormat {
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
  private static class PostMakeApiOutputFormat {
    
    @NonNull
    String output_name;
    
    @NonNull
    String user_define_output_name;
    
    String description;
    
  }
  
  // TODO
  
  public Optional<PostMakeApiOutput> postMakeApi(String modelId, PostMakeApiInput input) {
    PostMakeApiOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, AnalysisServer.PYTHON, MAKE_API_PATH, modelId);
      
      final List<PostMakeApiInputFormat> inputFields = input.getInputFields()
                                                            .stream()
                                                            .map(inputFormat -> {
                                                              return PostMakeApiInputFormat.builder()
                                                                                           .feature_name(
                                                                                               inputFormat.getFeatureName())
                                                                                           .user_define_feature_name(
                                                                                               inputFormat.getUserDefineFeatureName())
                                                                                           .type(inputFormat.getType())
                                                                                           .description(
                                                                                               inputFormat.getDescription())
                                                                                           .build();
                                                            })
                                                            .collect(Collectors.toList());
      final List<PostMakeApiOutputFormat> outputFields = input.getOutputFields()
                                                              .stream()
                                                              .map(outputFormat -> {
                                                                return PostMakeApiOutputFormat.builder()
                                                                                              .output_name(
                                                                                                  outputFormat.getOutputName())
                                                                                              .user_define_output_name(
                                                                                                  outputFormat.getUserDefineOutpuName())
                                                                                              .description(
                                                                                                  outputFormat.getDescription())
                                                                                              .build();
                                                              })
                                                              .collect(Collectors.toList());
      final PostMakeApiRequest req = PostMakeApiRequest.builder()
                                                       .user_id(input.getUserId())
                                                       .api_name(input.getApiName())
                                                       .api_description(input.getApiDescription())
                                                       .input_field(inputFields)
                                                       .output_field(outputFields)
                                                       .build();
      
      final HttpEntity<PostMakeApiRequest> reqEntity = new HttpEntity<PostMakeApiRequest>(req);
      final ResponseEntity<PostMakeApiResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, reqEntity,
          PostMakeApiResponse.class);
      log.debug("resEntity.getStatusCode()={}, resEntity.hasBody()={}", resEntity.getStatusCode(), resEntity.hasBody());
      if (HttpStatus.CREATED.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        PostMakeApiResponse res = resEntity.getBody();
        
        result = PostMakeApiOutput.builder()
                                  .status(res.getStatus())
                                  .description(res.getDescription())
                                  .modelId(res.getModel_id())
                                  .apiId(res.getApi_id())
                                  .apiName(res.getApi_name())
                                  .apiPath(res.getApi_path())
                                  .apiDescription(res.getApi_description())
                                  .inputFormats(res.getInput_format()
                                                   .stream()
                                                   .map(inputFormat -> {
                                                     return MakeApiInputFormat.builder()
                                                                              .featureName(
                                                                                  inputFormat.getFeature_name())
                                                                              .userDefineFeatureName(
                                                                                  inputFormat.getUser_define_feature_name())
                                                                              .type(inputFormat.getType())
                                                                              .description(inputFormat.getDescription())
                                                                              .build();
                                                   })
                                                   .collect(Collectors.toList()))
                                  .outputFormats(res.getOutput_format()
                                                    .stream()
                                                    .map(outputFormat -> {
                                                      return MakeApiOutputFormat.builder()
                                                                                .outputName(
                                                                                    outputFormat.getOutput_name())
                                                                                .userDefineOutpuName(
                                                                                    outputFormat.getUser_define_output_name())
                                                                                .description(
                                                                                    outputFormat.getDescription())
                                                                                .build();
                                                    })
                                                    .collect(Collectors.toList()))
                                  .build();
        
        log.debug("postMakeApi url : {}", serverUrl);
        log.debug("postMakeApi result={}", result);
      }
    } catch (Exception e) {
      log.error("postMakeApi error: {}", ExceptionUtils.getMessage(e), e);
    }
    
    log.info("postMakeApi[modelId={}] result={}", modelId, result);
    return Optional.ofNullable(result);
  }
}
