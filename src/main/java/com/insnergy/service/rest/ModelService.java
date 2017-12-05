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
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ModelInfo.ModelInfoAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelService {
  
  private static final String API_PATH = "/model";
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public ModelService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  // FIXME MISSING projectType and Label
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetModelOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<ModelListOutput> modelList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ModelListOutput {
    @NonNull
    String modelId;
    
    @NonNull
    String userId;
    
    @NonNull
    String projectId;
    
    @NonNull
    String projectType;
    
    @NonNull
    String modelName;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    List<ModelActionOutput> actions;
    
    @NonNull
    List<String> columnNames;
    
    String label;
    
    List<String> outputFormat;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ModelActionOutput {
    String function;
    String value;
    String userId;
    String stage;
    String projectId;
    Long timestamp;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetModelResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    List<ModelResponse> model_list;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class ModelResponse {
    @NonNull
    String model_id;
    
    @NonNull
    String user_id;
    
    @NonNull
    String project_id;
    
    @NonNull
    String project_type;
    
    @NonNull
    String model_name;
    
    @NonNull
    String model_method;
    
    @NonNull
    List<ModelActionResponse> actions;
    
    @NonNull
    List<String> column_names;
    
    String label;
    
    List<String> output_format;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class ModelActionResponse {
    String function;
    String value;
    String user_id;
    String stage;
    String project_id;
    Long timestamp;
  }
  
  // TODO
  // reference CsvServic getCsv
  public Optional<GetModelOutput> getModel(@NonNull AnalysisServer server, @NonNull String modelId) {
    GetModelOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, modelId);
      final ResponseEntity<GetModelResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetModelResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        GetModelResponse res = resEntity.getBody();
        result = GetModelOutput.builder()
                               .status(res.getStatus())
                               .description(res.getDescription())
                               .modelList(res.getModel_list()
                                             .stream()
                                             .map(model -> {
                                               return ModelListOutput.builder()
                                                                     .modelId(model.getModel_id())
                                                                     .userId(model.getUser_id())
                                                                     .projectId(model.getProject_id())
                                                                     .projectType(model.getProject_type())
                                                                     .modelName(model.getModel_name())
                                                                     .modelMethod(model.getModel_method())
                                                                     .actions(model.getActions()
                                                                                   .stream()
                                                                                   .map(action -> {
                                                                                     return ModelActionOutput.builder()
                                                                                                             .function(
                                                                                                                 action.getFunction())
                                                                                                             .value(
                                                                                                                 action.getValue())
                                                                                                             .userId(
                                                                                                                 action.getUser_id())
                                                                                                             .stage(
                                                                                                                 action.getStage())
                                                                                                             .projectId(
                                                                                                                 action.getProject_id())
                                                                                                             .timestamp(
                                                                                                                 action.getTimestamp())
                                                                                                             .build();
                                                                                   })
                                                                                   .collect(Collectors.toList()))
                                                                     .columnNames(model.getColumn_names())
                                                                     .label(model.getLabel())
                                                                     .outputFormat(model.getOutput_format())
                                                                     .build();
                                             })
                                             .collect(Collectors.toList()))
                               .build();
        
        log.debug("getSingleCsv url : {}", serverUrl);
        log.debug("getModelById result={}", result);
      }
    } catch (Exception e) {
      log.error("getModel error: {}", ExceptionUtils.getMessage(e), e);
      
    }
    log.info("getModel[modelId={}] result={}", modelId, result);
    return Optional.ofNullable(result);
  }
  
  public Optional<ModelInfo> buildModelInfo(@NonNull AnalysisServer server, @NonNull final ModelResponse model) {
    ModelInfo result = null;
    try {
      final List<ModelInfoAction> actions = model.getActions()
                                                 .stream()
                                                 .map(action -> {
                                                   return ModelInfoAction.builder()
                                                                         .userId(action.getUser_id())
                                                                         .function(action.getFunction())
                                                                         .value(action.getValue())
                                                                         .stage(action.getStage())
                                                                         .timestamp(action.getTimestamp())
                                                                         .build();
                                                 })
                                                 .collect(Collectors.toList());
      result = ModelInfo.builder()
                        .server(server)
                        .userId(model.getUser_id())
                        .modelId(model.getModel_id())
                        .projectId(model.getProject_id())
                        .projectType(model.getProject_type())
                        .modelMethod(model.getModel_method())
                        .actions(actions)
                        .columnNames(model.getColumn_names())
                        .label(model.getLabel())
                        .outputFormat(model.getOutput_format())
                        .build();
      
      log.debug("getCsvInfo result:{}", result);
    } catch (Exception e) {
      log.error("buildCvsFileInfo error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  /**
   * <CURD> DELETE MODEL
   * 
   * @author Alice
   * 
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DeleteModelResponse {
    String status;
    String description;
  }
  
  public Optional<DeleteModelResponse> deleteModel(@NonNull AnalysisServer server, @NonNull String modelId) {
    DeleteModelResponse result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, modelId);
      log.debug("DeleteModelUrl={}", serverUrl);
      
      final ResponseEntity<DeleteModelResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.DELETE,
          HttpEntity.EMPTY, DeleteModelResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        result = resEntity.getBody();
      }
      
    } catch (Exception e) {
      log.error("deleteModel error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  /**
   * <CURD> UPDATE MODEL
   * 
   * @author Alice
   * 
   */
  // TODO
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutModelInput {
    String modelName;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PutModelOutput {
    
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String modelId;
    
    @NonNull
    String userId;
    
    @NonNull
    String projectId;
    
    @NonNull
    String projectType;
    
    @NonNull
    String modelName;
    
    @NonNull
    String modelMethod;
    
    @NonNull
    List<ModelActionOutput> actions;
    
    @NonNull
    List<String> columnNames;
    String label;
    List<String> outputFormat;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutModelRequest {
    String model_name;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PutModelResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    @NonNull
    String model_id;
    
    @NonNull
    String user_id;
    
    @NonNull
    String project_id;
    
    @NonNull
    String project_type;
    
    @NonNull
    String model_name;
    
    @NonNull
    String model_method;
    
    @NonNull
    List<ModelActionResponse> actions;
    
    @NonNull
    List<String> column_names;
    
    String label;
    
    List<String> output_format;
  }
  
  public Optional<PutModelOutput> updateModel(@NonNull AnalysisServer server, @NonNull String modelId,
      @NonNull PutModelInput input) {
    PutModelOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, modelId);
      log.debug("updateModelUrl={},modelId={},modelInput={}", serverUrl, modelId, input.getModelName());
      
      final PutModelRequest req = PutModelRequest.builder()
                                                 .model_name(input.getModelName())
                                                 .build();
      
      final HttpEntity<PutModelRequest> reqEntity = new HttpEntity<PutModelRequest>(req);
      final ResponseEntity<PutModelResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.PUT, reqEntity,
          PutModelResponse.class);
      log.debug("resEntity.getStatusCode()={}, resEntity.hasBody()={}", resEntity.getStatusCode(), resEntity.hasBody());
      
      // TODO
      if (HttpStatus.CREATED.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        PutModelResponse res = resEntity.getBody();
        
        result = PutModelOutput.builder()
                               .modelId(res.getModel_id())
                               .userId(res.getUser_id())
                               .projectId(res.getProject_id())
                               .projectType(res.getProject_type())
                               .modelName(res.getModel_name())
                               .modelMethod(res.getModel_method())
                               .actions(res.getActions()
                                           .stream()
                                           .map(action -> {
                                             return ModelActionOutput.builder()
                                                                     .function(action.getFunction())
                                                                     .value(action.getValue())
                                                                     .userId(action.getUser_id())
                                                                     .stage(action.getStage())
                                                                     .projectId(action.getProject_id())
                                                                     .timestamp(action.getTimestamp())
                                                                     .build();
                                           })
                                           .collect(Collectors.toList()))
                               .columnNames(res.getColumn_names())
                               .label(res.getLabel())
                               .outputFormat(res.getOutput_format())
                               .build();
        
        log.debug("UpdateModel_resultForOutput={}", result);
      }
    } catch (
    
    Exception e) {
      log.error("PutModel error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
    
  }
  
}
