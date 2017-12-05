package com.insnergy.service.rest;

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

/**
 * * Model Training <AbnormalDetection> <Regression> <Classification> Model
 * Preview
 * 
 * @author Alice
 *
 */
@Service
@Slf4j
public class ModelTrainingService {
  
  private static final String TRAINING_API_PATH = "/model-training";
  private static final String PREVIEW_API_PATH = "/model-preview";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public ModelTrainingService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  /**
   * 
   * Model Training Response
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelTrainingResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String model_id;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostModelTrainingInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String fileId;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    String modelName;
    
    @NonNull
    ArgumentOfOneClassSVM argument;
    
    String projectType;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostModelTrainingOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String modelId;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelTrainingRequest {
    @NonNull
    String model_method;
    
    @NonNull
    String model_name;
    
    @NonNull
    ArgumentOfOneClassSVM argument;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ArgumentOfOneClassSVM {
    Double gamma;
    
    Double nu;
    
    String kernel;
    
    Integer degree;
  }
  
  /**
   * 
   * Model Training <AbnormalDetection>one-class_SVM
   */
  public Optional<PostModelTrainingOutput> postModelTraining(@NonNull PostModelTrainingInput input) {
    PostModelTrainingOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), TRAINING_API_PATH,
          input.getProjectType(), input.getFileId());
      log.debug("postModelTraining_serverUrl={},projectType={}", serverUrl, input.getProjectType());
      
      final ArgumentOfOneClassSVM argument = ArgumentOfOneClassSVM.builder()
                                                                  .gamma(input.getArgument()
                                                                              .getGamma())
                                                                  .nu(input.getArgument()
                                                                           .getNu())
                                                                  .kernel(input.getArgument()
                                                                               .getKernel())
                                                                  .degree(input.getArgument()
                                                                               .getDegree())
                                                                  .build();
      
      final PostModelTrainingRequest req = PostModelTrainingRequest.builder()
                                                                   .model_method(input.getModelMethod())
                                                                   .model_name(input.getModelName())
                                                                   .argument(argument)
                                                                   .build();
      
      final HttpEntity<PostModelTrainingRequest> reqEntity = new HttpEntity<PostModelTrainingRequest>(req);
      final ResponseEntity<PostModelTrainingResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostModelTrainingResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostModelTrainingResponse res = resEntity.getBody();
        result = PostModelTrainingOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .modelId(res.getModel_id())
                                        .build();
      }
    } catch (Exception e) {
      log.error("postOne_Class_SVM_ModelTraining error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostRegressionModelTrainingInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String fileId;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    String modelName;
    
    @NonNull
    ArgumentOfLinearRegression argument;
    
    String projectType;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostRegressionModelTrainingRequest {
    @NonNull
    String model_method;
    
    @NonNull
    String model_name;
    
    @NonNull
    ArgumentOfLinearRegression argument;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ArgumentOfLinearRegression {
    Boolean fit_intercept;
    
    Boolean normalize;
    
    Boolean copy_X;
    
    Integer n_jobs;
  }
  
  /**
   * 
   * Model Training <Regression>Linear Regression
   */
  
  public Optional<PostModelTrainingOutput> postRegressionModelTraining(
      @NonNull PostRegressionModelTrainingInput input) {
    PostModelTrainingOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), TRAINING_API_PATH,
          input.getProjectType(), input.getFileId());
      log.debug("postModelTraining_serverUrl={},projectType={}", serverUrl, input.getProjectType());
      
      final ArgumentOfLinearRegression argument = ArgumentOfLinearRegression.builder()
                                                                            .fit_intercept(input.getArgument()
                                                                                                .getFit_intercept())
                                                                            .copy_X(input.getArgument()
                                                                                         .getCopy_X())
                                                                            .normalize(input.getArgument()
                                                                                            .getNormalize())
                                                                            .n_jobs(input.getArgument()
                                                                                         .getN_jobs())
                                                                            .build();
      
      final PostRegressionModelTrainingRequest req = PostRegressionModelTrainingRequest.builder()
                                                                                       .model_method(
                                                                                           input.getModelMethod())
                                                                                       .model_name(input.getModelName())
                                                                                       .argument(argument)
                                                                                       .build();
      
      final HttpEntity<PostRegressionModelTrainingRequest> reqEntity = new HttpEntity<PostRegressionModelTrainingRequest>(
          req);
      final ResponseEntity<PostModelTrainingResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostModelTrainingResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostModelTrainingResponse res = resEntity.getBody();
        result = PostModelTrainingOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .modelId(res.getModel_id())
                                        .build();
      }
    } catch (Exception e) {
      log.error("postRegressionModelTraining error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostClassificationModelTrainingInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String fileId;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    String modelName;
    
    @NonNull
    ArgumentOfKnnInput argument;
    
    String projectType;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ArgumentOfKnnInput {
    @NonNull
    Integer nNeighbors;
    @NonNull
    String weights;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostClassificationModelTrainingRequest {
    @NonNull
    String model_method;
    
    @NonNull
    String model_name;
    
    @NonNull
    ArgumentOfKnnRequest argument;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class ArgumentOfKnnRequest {
    @NonNull
    Integer n_neighbors;
    @NonNull
    String weights;
  }
  
  /**
   * 
   * Model Training <Classification>
   */
  public Optional<PostModelTrainingOutput> postClassificationModelTraining(
      @NonNull PostClassificationModelTrainingInput input) {
    PostModelTrainingOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), TRAINING_API_PATH,
          input.getProjectType(), input.getFileId());
      log.debug("postModelTraining_serverUrl={},projectType={}", serverUrl, input.getProjectType());
      
      final ArgumentOfKnnRequest argument = ArgumentOfKnnRequest.builder()
                                                                .n_neighbors(input.getArgument()
                                                                                  .getNNeighbors())
                                                                .weights(input.getArgument()
                                                                              .getWeights())
                                                                .build();
      
      final PostClassificationModelTrainingRequest req = PostClassificationModelTrainingRequest.builder()
                                                                                               .model_method(
                                                                                                   input.getModelMethod())
                                                                                               .model_name(
                                                                                                   input.getModelName())
                                                                                               .argument(argument)
                                                                                               .build();
      
      final HttpEntity<PostClassificationModelTrainingRequest> reqEntity = new HttpEntity<PostClassificationModelTrainingRequest>(
          req);
      final ResponseEntity<PostModelTrainingResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostModelTrainingResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostModelTrainingResponse res = resEntity.getBody();
        result = PostModelTrainingOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .modelId(res.getModel_id())
                                        .build();
      }
    } catch (Exception e) {
      log.error("postClassificationModelTraining error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  // TODO
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostClusteringModelTrainingInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String fileId;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    String modelName;
    
    @NonNull
    ArgumentOfKmeansInput argument;
    
    String projectType;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ArgumentOfKmeansInput {
    @NonNull
    Integer nClusters;
    
    @NonNull
    Integer nInit;
    
    @NonNull
    Integer maxIter;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostClusteringModelTrainingRequest {
    @NonNull
    String model_method;
    
    @NonNull
    String model_name;
    
    @NonNull
    ArgumentOfKmeansRequest argument;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class ArgumentOfKmeansRequest {
    @NonNull
    Integer n_clusters;
    
    @NonNull
    Integer n_init;
    
    @NonNull
    Integer max_iter;
  }
  
  /**
   * 
   * Model Training <Clustering>
   */
  public Optional<PostModelTrainingOutput> postClusteringModelTraining(
      @NonNull PostClusteringModelTrainingInput input) {
    PostModelTrainingOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), TRAINING_API_PATH,
          input.getProjectType(), input.getFileId());
      log.debug("postModelTraining_serverUrl={},projectType={}", serverUrl, input.getProjectType());
      
      final ArgumentOfKmeansRequest argument = ArgumentOfKmeansRequest.builder()
                                                                      .n_clusters(input.getArgument()
                                                                                       .getNClusters())
                                                                      .n_init(input.getArgument()
                                                                                   .getNInit())
                                                                      .max_iter(input.getArgument()
                                                                                     .getMaxIter())
                                                                      .build();
      
      final PostClusteringModelTrainingRequest req = PostClusteringModelTrainingRequest.builder()
                                                                                       .model_method(
                                                                                           input.getModelMethod())
                                                                                       .model_name(input.getModelName())
                                                                                       .argument(argument)
                                                                                       .build();
      
      final HttpEntity<PostClusteringModelTrainingRequest> reqEntity = new HttpEntity<PostClusteringModelTrainingRequest>(
          req);
      final ResponseEntity<PostModelTrainingResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostModelTrainingResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostModelTrainingResponse res = resEntity.getBody();
        result = PostModelTrainingOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .modelId(res.getModel_id())
                                        .build();
      }
    } catch (Exception e) {
      log.error("PostClusteringModelTraining error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  /*
   * Model Preview
   */
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostModelPreviewInput {
    @NonNull
    AnalysisServer server;
    
    @NonNull
    String fileId;
    
    @NonNull
    String modelId;
    
    @NonNull
    String xAxis;
    
    @NonNull
    String yAxis;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostModelPreviewOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String imageUrl;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelPreviewRequest {
    String model_id;
    String x_axis;
    String y_axis;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostModelPreviewResponse {
    String status;
    String description;
    String image_path;
  }
  
  public Optional<PostModelPreviewOutput> postModelPreview(@NonNull PostModelPreviewInput input) {
    PostModelPreviewOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), PREVIEW_API_PATH,
          input.getFileId());
      
      final PostModelPreviewRequest req = PostModelPreviewRequest.builder()
                                                                 .model_id(input.getModelId())
                                                                 .x_axis(input.getXAxis())
                                                                 .y_axis(input.getYAxis())
                                                                 .build();
      
      final HttpEntity<PostModelPreviewRequest> reqEntity = new HttpEntity<PostModelPreviewRequest>(req);
      final ResponseEntity<PostModelPreviewResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST,
          reqEntity, PostModelPreviewResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostModelPreviewResponse res = resEntity.getBody();
        final String imageUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), res.getImage_path());
        result = PostModelPreviewOutput.builder()
                                       .status(res.getStatus())
                                       .description(res.getDescription())
                                       .imageUrl(imageUrl)
                                       .build();
      }
    } catch (Exception e) {
      log.error("postModelPreview error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
