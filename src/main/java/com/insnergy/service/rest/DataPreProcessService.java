package com.insnergy.service.rest;

import java.util.List;
import java.util.Optional;

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
public class DataPreProcessService {
  
  private static final String PREVIEW_API_PATH = "/preprocess-preview";
  private static final String SAVE_API_PATH = "/preprocess-save";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public DataPreProcessService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessPreviewRequest {
    String feature_name;
    int filter_std;
    boolean missing_value;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessPreviewResponse {
    String status;
    String description;
    String feature_name;
    List<ValueCountPair> value_list;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ValueCountPair {
      Double value;
      Integer count;
    }
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessSaveRequest {
    @NonNull
    String new_file_name;
    
    String normalize;
    
    List<String> feature_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessSaveResponse {
    String status;
    String description;
    String new_file_id;
  }
  
  public Optional<PostPreProcessSaveResponse> postPreProcessSave(AnalysisServer server, String fileId,
      String newFileName, String normalize, List<String> feature_list) {
    PostPreProcessSaveResponse result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, SAVE_API_PATH, fileId);
      
      final PostPreProcessSaveRequest req = PostPreProcessSaveRequest.builder()
                                                                     .new_file_name(newFileName)
                                                                     .normalize(normalize)
                                                                     .feature_list(feature_list)
                                                                     .build();
      
      final HttpEntity<PostPreProcessSaveRequest> reqEntity = new HttpEntity<PostPreProcessSaveRequest>(req);
      final ResponseEntity<PostPreProcessSaveResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostPreProcessSaveResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        result = resEntity.getBody();
      }
    } catch (Exception e) {
      log.error("postPreProcessSave error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessViewerInput {
    String featureName;
    Integer filterStd;
    Boolean missingValue;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostPreProcessViewerOutput {
    String status;
    String description;
    String featureName;
    String tmpFileId;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostPreProcessViewerRequest {
    @NonNull
    String feature_name;
    
    Integer filter_std;
    
    Boolean missing_value;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostPreProcessViewerResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String feature_name;
    
    @NonNull
    String tmp_file_id;
    
  }
  
  public Optional<PostPreProcessViewerOutput> postPreProcessDataViewer(AnalysisServer server, String fileId,
      PostPreProcessViewerInput input) {
    PostPreProcessViewerOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, PREVIEW_API_PATH, fileId);
      
      final PostPreProcessViewerRequest req = PostPreProcessViewerRequest.builder()
                                                                         .feature_name(input.getFeatureName())
                                                                         .filter_std(input.getFilterStd())
                                                                         .missing_value(input.getMissingValue())
                                                                         .build();
      
      final HttpEntity<PostPreProcessViewerRequest> reqEntity = new HttpEntity<PostPreProcessViewerRequest>(req);
      
      final ResponseEntity<PostPreProcessViewerResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostPreProcessViewerResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        PostPreProcessViewerResponse res = resEntity.getBody();
        result = PostPreProcessViewerOutput.builder()
                                           .status(res.getStatus())
                                           .description(res.getDescription())
                                           .featureName(res.getFeature_name())
                                           .tmpFileId(res.getTmp_file_id())
                                           .build();
        
      }
    } catch (Exception e) {
      log.error("PostPreProcessViewer error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
