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
public class ModelPredictionService {
  
  private static final String SEARCH_API_PATH = "/model-search";
  private static final String PREDICT_API_PATH = "/model-predict";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public ModelPredictionService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetModelSearchInput {
    @NonNull
    AnalysisServer server;
  }
  

  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetModelSearchOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<GetSearchModel> model_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetModelSearchResponse {
    String status;
    String description;
    List<GetSearchModel> model_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetSearchModel {
    String model_id;
    String model_name;
    String model_method;
    String user_id;
    String project_id;
    String project_type;
    String label;
    List<GetSearchModelAction> actions;
    List<String> column_names;
    List<String> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetSearchModelAction {
    String function;
    String value;
    String user_id;
    String stage;
    Long timestamp;
  }
  
  /**
   * 
   * Model Search<AbnormalDetection><Regression><Classification>
   */
  
  // TODO PYTHON model search API changed
  public Optional<GetModelSearchOutput> getModelSearch(@NonNull GetModelSearchInput input, @NonNull String fileId) {
    GetModelSearchOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), SEARCH_API_PATH, fileId);
      final ResponseEntity<GetModelSearchResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetModelSearchResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetModelSearchResponse res = resEntity.getBody();
        
        result = GetModelSearchOutput.builder()
                                     .status(res.getStatus())
                                     .description(res.getDescription())
                                     .model_list(res.getModel_list()
                                                    .stream()
                                                    .map(this::toGetSearchModelOutput)
                                                    .collect(Collectors.toList()))
                                     .build();
      }
    } catch (Exception e) {
      log.error("postModelSearch error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  // TODO PYTHON model search API changed
  GetSearchModel toGetSearchModelOutput(GetSearchModel model) {
    List<GetSearchModelAction> actions = model.getActions()
                                              .stream()
                                              .map(action -> {
                                                return GetSearchModelAction.builder()
                                                                           .user_id(action.getUser_id())
                                                                           .stage(action.getStage())
                                                                           .function(action.getFunction())
                                                                           .timestamp(action.getTimestamp())
                                                                           .value(action.getValue())
                                                                           .build();
                                              })
                                              .collect(Collectors.toList());
    return GetSearchModel.builder()
                         .model_id(model.getModel_id())
                         .model_name(model.getModel_name())
                         .model_method(model.getModel_method())
                         .user_id(model.getUser_id())
                         .project_id(model.getProject_id())
                         .project_type(model.getProject_type())
                         .label(model.getLabel())
                         .column_names(model.getColumn_names())
                         .output_format(model.getOutput_format())
                         .actions(actions)
                         .build();
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  
  public static class PostModelPredictInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String modelId;
    
    @NonNull
    String fileId;
    
    @NonNull
    String projectType;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostModelPredictOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    ModelPredictConfusionMatrix confusionMatrix;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelPredictRequest {
    String model_id;
    
    String file_id;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelPredictResponse {
    String status;
    
    String description;
    
    ModelPredictConfusionMatrix confusion_matrix;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ModelPredictConfusionMatrix {
    int tp;
    int fn;
    int fp;
    int tn;
  }
  
  /**
   * 
   * Model Predict Page<AbnormalDetection>
   */
  // FIXME
  public Optional<PostClassificationModelPredictOutput> postModelPredict(@NonNull PostModelPredictInput input) {
    PostClassificationModelPredictOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), PREDICT_API_PATH,
          input.projectType);
      
      final PostModelPredictRequest req = PostModelPredictRequest.builder()
                                                                 .model_id(input.getModelId())
                                                                 .file_id(input.getFileId())
                                                                 .build();
      
      final HttpEntity<PostModelPredictRequest> reqEntity = new HttpEntity<PostModelPredictRequest>(req);
      final ResponseEntity<PostClassificationModelPredictResponse> resEntity = restTemplate.exchange(serverUrl,
          HttpMethod.POST, reqEntity, PostClassificationModelPredictResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostClassificationModelPredictResponse res = resEntity.getBody();
        final ClassificationModelPerformanceOutput performanceOutput = ClassificationModelPerformanceOutput.builder()
                                                                                                           .classificationReport(
                                                                                                               res.getPerformance()
                                                                                                                  .getClassification_report())
                                                                                                           .confusionMatrix(
                                                                                                               res.getPerformance()
                                                                                                                  .getConfusion_matrix())
                                                                                                           .build();
        result = PostClassificationModelPredictOutput.builder()
                                                     .status(res.getStatus())
                                                     .description(res.getDescription())
                                                     .performance(performanceOutput)
                                                     .build();
      }
    } catch (Exception e) {
      log.error("postModelPredict error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostRegressionModelPredictOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    RegressionModelPredictPerformance performance;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostRegressionModelPredictResponse {
    String status;
    
    String description;
    
    RegressionModelPredictPerformance performance;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class RegressionModelPredictPerformance {
    float mean_square_error;
  }
  
  /**
   * 
   * Model Predict<Regression>
   */
  public Optional<PostRegressionModelPredictOutput> postRegressionModelPredict(@NonNull PostModelPredictInput input) {
    PostRegressionModelPredictOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), PREDICT_API_PATH,
          input.projectType);
      
      final PostModelPredictRequest req = PostModelPredictRequest.builder()
                                                                 .model_id(input.getModelId())
                                                                 .file_id(input.getFileId())
                                                                 .build();
      
      final HttpEntity<PostModelPredictRequest> reqEntity = new HttpEntity<PostModelPredictRequest>(req);
      final ResponseEntity<PostRegressionModelPredictResponse> resEntity = restTemplate.exchange(serverUrl,
          HttpMethod.POST, reqEntity, PostRegressionModelPredictResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostRegressionModelPredictResponse res = resEntity.getBody();
        result = PostRegressionModelPredictOutput.builder()
                                                 .status(res.getStatus())
                                                 .description(res.getDescription())
                                                 .performance(res.getPerformance())
                                                 .build();
      }
    } catch (Exception e) {
      log.error("postRegressionModelPredictPredict error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostClassificationModelPredictOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    ClassificationModelPerformanceOutput performance;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ClassificationModelPerformanceOutput {
    @NonNull
    String classificationReport;
    @NonNull
    String confusionMatrix;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostClassificationModelPredictResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    ClassificationModelPerformance performance;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class ClassificationModelPerformance {
    @NonNull
    String classification_report;
    @NonNull
    String confusion_matrix;
  }
  
  /**
   * 
   * Model Predict Page<Classification>
   */
  // TODO
  public Optional<PostClassificationModelPredictOutput> postClassificationModelPredict(
      @NonNull PostModelPredictInput input) {
    PostClassificationModelPredictOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), PREDICT_API_PATH,
          input.projectType);
      
      final PostModelPredictRequest req = PostModelPredictRequest.builder()
                                                                 .model_id(input.getModelId())
                                                                 .file_id(input.getFileId())
                                                                 .build();
      // TODO reduce duplicated code by changing to switch case(projectType)
      final HttpEntity<PostModelPredictRequest> reqEntity = new HttpEntity<PostModelPredictRequest>(req);
      final ResponseEntity<PostClassificationModelPredictResponse> resEntity = restTemplate.exchange(serverUrl,
          HttpMethod.POST, reqEntity, PostClassificationModelPredictResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostClassificationModelPredictResponse res = resEntity.getBody();
        final ClassificationModelPerformanceOutput performanceOutput = ClassificationModelPerformanceOutput.builder()
                                                                                                           .classificationReport(
                                                                                                               res.getPerformance()
                                                                                                                  .getClassification_report())
                                                                                                           .confusionMatrix(
                                                                                                               res.getPerformance()
                                                                                                                  .getConfusion_matrix())
                                                                                                           .build();
        result = PostClassificationModelPredictOutput.builder()
                                                     .status(res.getStatus())
                                                     .description(res.getDescription())
                                                     .performance(performanceOutput)
                                                     .build();
      }
    } catch (Exception e) {
      log.error("postClassificationModelPredictPredict error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
