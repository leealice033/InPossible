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
public class DataViewerService {
  private static final String VIEWER_PATH = "/data-viewer";// /data-viewer/{file_id}
  private static final String CHART_PAHT = "/data-chart";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public DataViewerService(@NonNull InAnalysisConfig config, @NonNull RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetViewerOutput {
    String status;
    String description;
    List<DataInfoOutput> dataInfo;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DataInfoOutput {
    String columnName;
    String firstValue;
    String valueType;
    String mean;
    String standardDeviation;
    String chartPath;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetViewerResponse {
    @NonNull
    String status;
    @NonNull
    String description;
    @NonNull
    List<DataInfo> data_info;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class DataInfo {
    @NonNull
    String column_name;
    @NonNull
    String first_value;
    @NonNull
    String value_type;
    @NonNull
    String mean;
    @NonNull
    String standard_deviation;
    @NonNull
    String chart_path;
    
  }
  
  public Optional<GetViewerOutput> getDataViewerByFileId(@NonNull AnalysisServer server, @NonNull String fileId) {
    GetViewerOutput result = null;
    try {
      final String url = AnalysisServerUtil.buildUrl(config, server, VIEWER_PATH, fileId);
      log.debug("getDataViewerByFileId url={}", url);
      
      final ResponseEntity<GetViewerResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
          GetViewerResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetViewerResponse res = resEntity.getBody();
        result = GetViewerOutput.builder()
                                .status(res.getStatus())
                                .description(res.getDescription())
                                .dataInfo(res.getData_info()
                                             .stream()
                                             .map(_dataInfo -> {
                                               return DataInfoOutput.builder()
                                                                    .columnName(_dataInfo.getColumn_name())
                                                                    .firstValue(_dataInfo.getFirst_value())
                                                                    .mean(_dataInfo.getMean())
                                                                    .valueType(_dataInfo.getValue_type())
                                                                    .standardDeviation(
                                                                        _dataInfo.getStandard_deviation())
                                                                    .chartPath(_dataInfo.getChart_path())
                                                                    .build();
                                             })
                                             .collect(Collectors.toList()))
                                .build();
      }
    } catch (Exception e) {
      log.error("getDataViewerByFileId error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.info("getDataViewerByFileId[{}]={}", fileId, result);
    return Optional.ofNullable(result);
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetViewerByColumnOutput {
    String status;
    String description;
    String imagePath;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetViewerByColumnResponse {
    @NonNull
    String status;
    @NonNull
    String description;
    @NonNull
    String image_path;
  }
  
  public Optional<GetViewerByColumnOutput> getDataViewerByColumn(@NonNull AnalysisServer server, @NonNull String fileId,
      @NonNull String columnName) {
    GetViewerByColumnOutput result = null;
    try {
      final String url = AnalysisServerUtil.buildUrl(config, server, CHART_PAHT, fileId, columnName);
      log.debug("getDataViewerByColumn url={}", url);
      
      final ResponseEntity<GetViewerByColumnResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET,
          HttpEntity.EMPTY, GetViewerByColumnResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetViewerByColumnResponse res = resEntity.getBody();
        result = GetViewerByColumnOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .imagePath(AnalysisServerUtil.buildUrl(config, server, res.getImage_path()))
                                        .build();
      }
    } catch (Exception e) {
      log.error("getDataViewerByColumn error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.info("getDataViewerByColumn[{}]={}", fileId, result);
    return Optional.ofNullable(result);
  }
  
  public Optional<GetViewerByColumnOutput> getDataChartByPath(@NonNull AnalysisServer server,
      @NonNull String dataImagePath) {
    GetViewerByColumnOutput result = null;
    try {
      final String url = AnalysisServerUtil.buildUrl(config, server, dataImagePath);
      log.debug("getDataChartByPath_url={}", url);
      final ResponseEntity<GetViewerByColumnResponse> resEntity = restTemplate.exchange(url, HttpMethod.GET,
          HttpEntity.EMPTY, GetViewerByColumnResponse.class);
      
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetViewerByColumnResponse res = resEntity.getBody();
        result = GetViewerByColumnOutput.builder()
                                        .status(res.getStatus())
                                        .description(res.getDescription())
                                        .imagePath(AnalysisServerUtil.buildUrl(config, server, res.getImage_path()))
                                        .build();
      }
    } catch (Exception e) {
      log.error("getDataChartByPath error: {}", ExceptionUtils.getMessage(e), e);
    }
    log.info("getDataChartByPath={}", result);
    return Optional.ofNullable(result);
  }
  
}
