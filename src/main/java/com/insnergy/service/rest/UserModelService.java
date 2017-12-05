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
import com.insnergy.web.ModelManagementController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

//TODO
@Service
@Slf4j
public class UserModelService {
  
  private static final String API_PATH = "/user";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public UserModelService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetUserModelOutput {
    String status;
    String description;
    List<ModelInfo> modelList;
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
  // FIXME project_type & label
  
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
  
  public Optional<GetUserModelOutput> getUserModel(@NonNull AnalysisServer server, @NonNull String userId) {
    return getUserProjectModel(server, userId, null);
  }
  
  public Optional<GetUserModelOutput> getUserProjectModel(@NonNull AnalysisServer server, @NonNull String userId,
      @NonNull String projectId) {
    GetUserModelOutput result = null;
    try {
      String url = AnalysisServerUtil.buildUrl(config, server, API_PATH, userId, "model");
      if (projectId != null) {
        url += ("/" + projectId);
      }
      log.debug("url={}", url);
      final ResponseEntity<GetModelResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
          GetModelResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        GetModelResponse res = resEntity.getBody();
        
        final List<ModelInfo> modelList = res.getModel_list()
                                             .stream()
                                             .map(model -> buildModelInfo(config, server, model))
                                             .filter(Optional::isPresent)
                                             .map(Optional::get)
                                             .collect(Collectors.toList());
        
        result = GetUserModelOutput.builder()
                                   .status(res.getStatus())
                                   .description(res.getDescription())
                                   .modelList(modelList)
                                   .build();
      }
    } catch (Exception e) {
      log.error("getModel error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  Optional<ModelInfo> buildModelInfo(@NonNull InAnalysisConfig config, @NonNull AnalysisServer server,
      @NonNull final ModelResponse model) {
    ModelInfo result = null;
    
    try {
      
      final List<ModelInfoAction> actions = model.getActions()
                                                 .stream()
                                                 .map(action -> {
                                                   return ModelInfoAction.builder()
                                                                         .userId(action.getUser_id())
                                                                         .timestamp(action.getTimestamp())
                                                                         .stage(action.getStage())
                                                                         .function(action.getFunction())
                                                                         .value(action.getValue())
                                                                         .build();
                                                 })
                                                 .collect(Collectors.toList());
      
      result = ModelInfo.builder()
                        .userId(model.getUser_id())
                        .server(server)
                        .modelId(model.getModel_id())
                        .modelName(model.getModel_name())
                        .modelMethod(model.getModel_method())
                        .deleteUrl(
                            ModelManagementController.getDeleteModelPath(model.getProject_id(), model.getModel_id()))
                        .projectId(model.getModel_id())
                        .projectType(model.getProject_type())
                        .columnNames(model.getColumn_names())
                        .label(model.getLabel())
                        .outputFormat(model.getOutput_format())
                        .actions(actions)
                        .build();
      
    } catch (Exception e) {
      log.error("buildModelInfo error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
}
