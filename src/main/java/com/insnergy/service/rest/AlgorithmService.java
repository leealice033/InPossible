package com.insnergy.service.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.insnergy.cofig.InAnalysisConfig;

import com.insnergy.util.AnalysisServer;
import com.insnergy.util.AnalysisServerUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlgorithmService {
  private static final String PATH = "/algo";
  private static final String LIST = "/list";
  private static final String DEF = "def";
  private static final String DO = "do";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public AlgorithmService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetAlgoByProjectTypeOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String projectType;
    
    @NonNull
    List<String> algoList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetAlgoByProjectTypeResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String project_type;
    
    @NonNull
    List<String> algo_list;
  }
  
  /// algo/list/{project_type}
  public Optional<GetAlgoByProjectTypeOutput> getAlgoByProjectType(AnalysisServer server, @NonNull String projectType) {
    GetAlgoByProjectTypeOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, PATH, LIST, projectType);
      log.debug("GetAlgoByProjectType_serverUrl={}", serverUrl);
      final ResponseEntity<GetAlgoByProjectTypeResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetAlgoByProjectTypeResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetAlgoByProjectTypeResponse res = resEntity.getBody();
        log.debug("GetAlgoByProjectTypeResponse_res={}", res);
        result = GetAlgoByProjectTypeOutput.builder()
                                           .status(res.getStatus())
                                           .description(res.getDescription())
                                           .projectType(res.getProject_type())
                                           .algoList(res.getAlgo_list())
                                           .build();
      }
      
    } catch (Exception e) {
      log.error("GetAlgoByProjectTypeOutput error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetAlgoParamOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String projectType;
    
    @NonNull
    List<AlgoParameterOutput> argumentsDef;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class AlgoParameterOutput {
    @NonNull
    String name;
    @NonNull
    String type;
    @NonNull
    String range;
    @NonNull
    String defaultValue;
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetAlgoParamResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String project_type;
    
    @NonNull
    List<AlgoParameterRes> arguments_def;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class AlgoParameterRes {
    @NonNull
    String name;
    @NonNull
    String type;
    @NonNull
    String range;
    @NonNull
    String default_value;
    String description;
  }
  
  /// algo/{algo_name}/def
  public Optional<GetAlgoParamOutput> getAlgoParameters(@NonNull AnalysisServer server, @NonNull String algoName) {
    GetAlgoParamOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, PATH, algoName, DEF);
      log.debug("GetAlgoParameter_serverUrl={}", serverUrl);
      final ResponseEntity<GetAlgoParamResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetAlgoParamResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetAlgoParamResponse res = resEntity.getBody();
        log.debug("GetAlgoParamResponse_res={}", res);
        result = GetAlgoParamOutput.builder()
                                   .status(res.getStatus())
                                   .description(res.getDescription())
                                   .projectType(res.getProject_type())
                                   .argumentsDef(res.getArguments_def()
                                                    .stream()
                                                    .map(paramRes -> {
                                                      return AlgoParameterOutput.builder()
                                                                                .name(paramRes.getName())
                                                                                .type(paramRes.getType())
                                                                                .range(paramRes.getRange())
                                                                                .defaultValue(
                                                                                    paramRes.getDefault_value())
                                                                                .description(paramRes.getDescription())
                                                                                .build();
                                                    })
                                                    .collect(Collectors.toList()))
                                   .build();
      }
      
    } catch (Exception e) {
      log.error("GetAlgoParamOutput error: {}", ExceptionUtils.getMessage(e), e);
    }
    
    return Optional.ofNullable(result);
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostDoAlgoInput {
    String projectType;
    String fileId;
    String modelMethod;
    String modelName;
    
    @Singular
    Map<String, String> arguments;// arguments
    
    String toJson() {
      Gson gson = new GsonBuilder().serializeNulls()
                                   .create();
      return gson.toJson(this);
    }
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostDoAlgoReq {
    String file_id;
    String model_name;
    
    @Singular
    Map<String, String> arguments;// arguments
    
    String toJson() {
      Gson gson = new GsonBuilder().serializeNulls()
                                   .create();
      return gson.toJson(this);
    }
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostDoAlgoOutput {
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
  private static class PostDoAlgoRes {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String model_id;
  }
  
  /// algo/{algo_name}/do
  public Optional<PostDoAlgoOutput> postDoAlgoModelTraining(@NonNull AnalysisServer server,
      @NonNull PostDoAlgoInput input) {
    PostDoAlgoOutput result = null;
    try {
      log.debug("input_modelMethod={}", input.getModelMethod());
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, PATH, input.getModelMethod(),DO);
      log.debug("postDoAlgoModelTraining_serverUrl={},projectType={}", serverUrl, input.getProjectType());
      
      final PostDoAlgoReq req = PostDoAlgoReq.builder()
                                             .file_id(input.getFileId())
                                             .model_name(input.getModelName())
                                             .arguments(input.getArguments())
                                             .build();
      
      final HttpEntity<PostDoAlgoReq> reqEntity = new HttpEntity<PostDoAlgoReq>(req);
      final ResponseEntity<PostDoAlgoRes> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, reqEntity,
          PostDoAlgoRes.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostDoAlgoRes res = resEntity.getBody();
        result = PostDoAlgoOutput.builder()
                                 .status(res.getStatus())
                                 .description(res.getDescription())
                                 .modelId(res.getModel_id())
                                 .build();
      }
    } catch (Exception e) {
      log.error("postDoAlgoModelTraining: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
