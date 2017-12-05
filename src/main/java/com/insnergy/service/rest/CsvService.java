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

import com.insnergy.api.CsvFileDownloadApi;
import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.AnalysisServerUtil;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.CsvInfo.CsvInfoAction;
import com.insnergy.vo.CsvInfo.CsvInfoRow;
import com.insnergy.web.ProjectController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvService {
  
  private static final String API_PATH = "/csv";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  private final CsvFileDownloadApi api;
  
  public CsvService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate,
      @NonNull CsvFileDownloadApi api) {
    this.config = config;
    this.restTemplate = restTemplate;
    this.api = api;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetCsvOutput {
    String status;
    String description;
    List<CsvInfo> csvList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetCsvResponse {
    String status;
    String description;
    List<CsvFileResponse> csv_files;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class CsvFileResponse {
    
    @NonNull
    String user_id;
    
    @NonNull
    String stage;
    
    @NonNull
    String file_id;
    
    @NonNull
    String file_name;
    
    @NonNull
    String project_id;
    
    @NonNull
    String project_type;
    
    @NonNull
    List<CsvFileActionResponse> actions;
    
    @NonNull
    List<Integer> dimension;
    
    @NonNull
    List<String> column_names;
    
    @NonNull
    String label;
    
    @NonNull
    List<CsvFileRowValueResponse> row_values;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class CsvFileActionResponse {
    
    @NonNull
    String function;
    
    @NonNull
    String value;
    
    @NonNull
    String user_id;
    
    @NonNull
    Long timestamp;
    
    @NonNull
    String stage;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class CsvFileRowValueResponse {
    
    @NonNull
    Integer index;
    
    @NonNull
    List<String> row_value;
  }
  
  public Optional<GetCsvOutput> getCsv(@NonNull AnalysisServer server, @NonNull String fileId) {
    GetCsvOutput result = null;
    try {
      final String url = AnalysisServerUtil.buildUrl(config, server, API_PATH, fileId);
      log.debug("getCsv url={}", url);
      
      final ResponseEntity<GetCsvResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
          GetCsvResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetCsvResponse res = resEntity.getBody();
        result = GetCsvOutput.builder()
                             .status(res.getStatus())
                             .description(res.getDescription())
                             .csvList(res.getCsv_files()
                                         .stream()
                                         .map(csvFile -> buildCvsFileInfo(config, server, csvFile))
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .collect(Collectors.toList()))
                             .build();
      }
    } catch (Exception e) {
      log.error("getCsv error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.info("getCsv[{}]={}", fileId, result);
    return Optional.ofNullable(result);
  }
  
  private static Optional<CsvInfo> buildCvsFileInfo(@NonNull InAnalysisConfig config, @NonNull AnalysisServer server,
      @NonNull final CsvFileResponse csvFile) {
    CsvInfo result = null;
    
    try {
      int row = -1;
      int column = -1;
      final List<Integer> dimension = csvFile.getDimension();
      if ((dimension != null) && (dimension.size() == 2)) {
        row = dimension.get(0);
        column = dimension.get(1);
      }
      
      final List<CsvInfoAction> actions = csvFile.getActions()
                                                 .stream()
                                                 .map(action -> {
                                                   return CsvInfoAction.builder()
                                                                       .userId(action.getUser_id())
                                                                       .timestamp(action.getTimestamp())
                                                                       .stage(action.getStage())
                                                                       .function(action.getFunction())
                                                                       .value(action.getValue())
                                                                       .build();
                                                 })
                                                 .collect(Collectors.toList());
      
      final List<CsvInfoRow> rows = csvFile.getRow_values()
                                           .stream()
                                           .map(rowinfo -> {
                                             return CsvInfoRow.builder()
                                                              .index(rowinfo.getIndex())
                                                              .rowValue(rowinfo.getRow_value())
                                                              .build();
                                           })
                                           .collect(Collectors.toList());
      result = CsvInfo.builder()
                      .server(server)
                      .downloadUrl(AnalysisServerUtil.getCsvFileDownloadUrl(config, server, csvFile.getFile_id()))
                      .deleteUrl(ProjectController.getDeleteFilePath(csvFile.getProject_id(), csvFile.getFile_id()))
                      .userId(csvFile.getUser_id())
                      .stage(csvFile.getStage())
                      .fileId(csvFile.getFile_id())
                      .fileName(csvFile.getFile_name())
                      .projectId(csvFile.getProject_id())
                      .projectType(csvFile.getProject_type())
                      .row(row)
                      .column(column)
                      .actions(actions)
                      .columnNames(csvFile.getColumn_names())
                      .label(csvFile.getLabel())
                      .rowValues(rows)
                      .build();
    } catch (Exception e) {
      log.error("buildCvsFileInfo error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostCsvInput {
    AnalysisServer server;
    String csvFileName;
    String userId;
    String projectId;
    String projectType;
    String stage;
    String label;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostCsvOutput {
    String status;
    String description;
    String fileId;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostCsvRequest {
    String download_url;
    String file_name;
    String user_id;
    String project_id;
    String project_type;
    String stage;
    String label;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostCsvResponse {
    String status;
    String description;
    String file_id;
  }
  
  public Optional<PostCsvOutput> postCsv(PostCsvInput input) {
    log.info("postCsv input={}", input);
    PostCsvOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, input.getServer(), API_PATH);
      final String downloadUrl = api.getDownloadFileUrl(input.getCsvFileName());
      
      final PostCsvRequest req = PostCsvRequest.builder()
                                               .download_url(downloadUrl)
                                               .file_name(input.getCsvFileName())
                                               .user_id(input.getUserId())
                                               .project_id(input.getProjectId())
                                               .project_type(input.getProjectType())
                                               .stage(input.getStage())
                                               .label(input.getLabel())
                                               .build();
      final HttpEntity<PostCsvRequest> reqEntity = new HttpEntity<PostCsvRequest>(req);
      
      final ResponseEntity<PostCsvResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, reqEntity,
          PostCsvResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        PostCsvResponse res = resEntity.getBody();
        result = PostCsvOutput.builder()
                              .status(res.getStatus())
                              .description(res.getDescription())
                              .fileId(res.getFile_id())
                              .build();
      }
    } catch (Exception e) {
      log.error("postCsv error={}", ExceptionUtils.getMessage(e), e);
    }
    log.info("postCsv result={}", result);
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DeleteCsvOutput {
    String status;
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class DeleteCsvResponse {
    String status;
    String description;
  }
  
  public Optional<DeleteCsvOutput> deleteCsv(@NonNull AnalysisServer server, @NonNull String fileId) {
    DeleteCsvOutput result = null;
    try {
      final String serverUrl = AnalysisServerUtil.buildUrl(config, server, API_PATH, fileId);
      final ResponseEntity<DeleteCsvResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.DELETE,
          HttpEntity.EMPTY, DeleteCsvResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final DeleteCsvResponse res = resEntity.getBody();
        result = DeleteCsvOutput.builder()
                                .status(res.getStatus())
                                .description(res.getDescription())
                                .build();
      }
    } catch (Exception e) {
      log.error("deleteCsv error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.info("deleteCsv[{}]={}", fileId, result);
    return Optional.ofNullable(result);
  }
  
}
