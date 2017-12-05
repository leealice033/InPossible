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
public class UserCsvService {
  
  private static final String API_PATH = "/user";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public UserCsvService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetUserCsvOutput {
    String status;
    String description;
    List<CsvInfo> csvList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetUserCsvResponse {
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
  
  public Optional<GetUserCsvOutput> getUserCsv(@NonNull AnalysisServer server, @NonNull String userId) {
    return getUserProjectCsv(server, userId, null, null);
  }
  
  public Optional<GetUserCsvOutput> getUserProjectCsv(@NonNull AnalysisServer server, @NonNull String userId,
      @NonNull String projectId) {
    return getUserProjectCsv(server, userId, projectId, null);
  }
  
  public Optional<GetUserCsvOutput> getUserProjectCsv(@NonNull AnalysisServer server, @NonNull String userId,
      String projectId, String stage) {
    GetUserCsvOutput result = null;
    try {
      String url = AnalysisServerUtil.buildUrl(config, server, API_PATH, userId, "csv");
      if (projectId != null) {
        url += ("/" + projectId);
      }
      if (projectId != null && stage != null) {
        url += ("/" + stage);
      }
      log.debug("url={}", url);
      
      final ResponseEntity<GetUserCsvResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
          GetUserCsvResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetUserCsvResponse res = resEntity.getBody();
        
        result = GetUserCsvOutput.builder()
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
  
}
