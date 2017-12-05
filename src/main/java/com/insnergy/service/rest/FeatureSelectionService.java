package com.insnergy.service.rest;

import java.util.ArrayList;
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
public class FeatureSelectionService {
  
  private static final String WEIGHT_API_PATH = "/feature-weight";
  private static final String SELECT_API_PATH = "/feature-select";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public FeatureSelectionService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostFeatureWeightInput {
    String fileId;
    String method;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostFeatureWeightOutput {
    String status;
    String description;
    List<String> featureList;
    List<FeatureAttributeOutput> featureAttribute;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class FeatureAttributeOutput {
    String featureName;
    Double mean;
    Double standardDeviation;
    Double averageCorrelation;
    List<Double> correlationList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostFeatureWeightRequest {
    String method;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostFeatureWeightResponse {
    String status;
    String description;
    List<String> feature_list;
    List<FeatureAttribute> feature_attribute;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class FeatureAttribute {
      String feature_name;
      Double mean;
      Double standard_deviation;
      Double average_correlation;
      List<Double> correlation_list;
    }
  }
  
  // TODO
  public Optional<PostFeatureWeightOutput> postFeatureWeight(AnalysisServer server, PostFeatureWeightInput input) {
    PostFeatureWeightOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, WEIGHT_API_PATH, input.fileId);
      log.debug("PostFeatureWeightOutput_serverUrl: {}", serverUrl);
      final PostFeatureWeightRequest req = PostFeatureWeightRequest.builder()
                                                                   .method(input.getMethod())
                                                                   .build();
      final HttpEntity<PostFeatureWeightRequest> reqEntity = new HttpEntity<PostFeatureWeightRequest>(req);
      final ResponseEntity<PostFeatureWeightResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostFeatureWeightResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostFeatureWeightResponse res = resEntity.getBody();
        List<FeatureAttributeOutput> _attribute = new ArrayList<>();
        _attribute = buildFeatureAttribute(config, server, res);
        result = PostFeatureWeightOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .featureList(res.getFeature_list())
                                        .featureAttribute(_attribute)
                                        .build();
        
        log.debug("PostFeatureWeightOutpu_result: {}", result);
        
      }
    } catch (
    
    Exception e) {
      log.error("postFeatureWeight error: {}", ExceptionUtils.getMessage(e), e);
    }
    
    return Optional.ofNullable(result);
  }
  
  // TODO
  private static List<FeatureAttributeOutput> buildFeatureAttribute(@NonNull InAnalysisConfig config,
      @NonNull AnalysisServer server, @NonNull final PostFeatureWeightResponse featureWeight) {
    List<FeatureAttributeOutput> result = featureWeight.getFeature_attribute()
                                                       .stream()
                                                       .map(attribute -> {
                                                         return FeatureAttributeOutput.builder()
                                                                                      .featureName(
                                                                                          attribute.getFeature_name())
                                                                                      .mean(attribute.getMean())
                                                                                      .standardDeviation(
                                                                                          attribute.getStandard_deviation())
                                                                                      .averageCorrelation(
                                                                                          attribute.getAverage_correlation())
                                                                                      .correlationList(
                                                                                          attribute.getCorrelation_list())
                                                                                      .build();
                                                       })
                                                       .collect(Collectors.toList());
    
    return result;
  }
  
  // TODO
  public static void setValue(@NonNull FeatureAttributeOutput attribute) {
    Double meanValue;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostFeatureSelectInput {
    String fileId;
    String newFileName;
    String stage;
    List<String> featureList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostFeatureSelectOutput {
    String status;
    String description;
    String newFileId;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostFeatureSelectRequest {
    String new_file_name;
    String stage;
    List<String> feature_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostFeatureSelectResponse {
    String status;
    String description;
    String new_file_id;
  }
  
  // TODO
  public Optional<PostFeatureSelectOutput> postFeatureSelect(AnalysisServer server, PostFeatureSelectInput input) {
    PostFeatureSelectOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, SELECT_API_PATH, input.fileId);
      log.debug("postFeatureSelectOutput_serverUrl={}", serverUrl);
      final PostFeatureSelectRequest req = PostFeatureSelectRequest.builder()
                                                                   .new_file_name(input.getNewFileName())
                                                                   .feature_list(input.getFeatureList())
                                                                   .stage(input.getStage())
                                                                   .build();
      
      final HttpEntity<PostFeatureSelectRequest> reqEntity = new HttpEntity<PostFeatureSelectRequest>(req);
      final ResponseEntity<PostFeatureSelectResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostFeatureSelectResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostFeatureSelectResponse res = resEntity.getBody();
        log.debug("postFeatureSelectOutput_Response={}", res);
        result = PostFeatureSelectOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .newFileId(res.getNew_file_id())
                                        .build();
        log.debug("postFeatureSelectOutput_OutputResults={}", result);
      }
      
    } catch (Exception e) {
      log.error("postFeatureSelect error: {}", ExceptionUtils.getMessage(e), e);
    }
    
    return Optional.ofNullable(result);
  }
  
}
