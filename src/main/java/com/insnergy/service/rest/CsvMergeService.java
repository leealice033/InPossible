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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvMergeService {
  
  // API_PATH = /csv-merge/{user_id}/{project_id}/{stage}
  private static final String API_PATH = "/csv-merge";
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public CsvMergeService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  // FIXME private Request and Response
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostCsvMergeRequest {
    String method;
    List<String> file_id_list;
    String merged_file_name;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostCsvMergeResponse {
    String status;
    String description;
    String new_file_id;
    List<String> dimension;
  }
  
  public Optional<PostCsvMergeResponse> postCsvMerge(AnalysisServer server, String userId, String projectId,
      String stage, String method, List<String> fileIdList, String mergedFileName) {
    PostCsvMergeResponse result = null;
    try {
      final String url = AnalysisServerUtil.buildUrl(config, server, API_PATH, userId, projectId, stage);
      log.debug("url={}", url);
      final PostCsvMergeRequest req = PostCsvMergeRequest.builder()
                                                         .method(method)
                                                         .file_id_list(fileIdList)
                                                         .merged_file_name(mergedFileName)
                                                         .build();
      final HttpEntity<PostCsvMergeRequest> reqEntity = new HttpEntity<PostCsvMergeRequest>(req);
      final ResponseEntity<PostCsvMergeResponse> resEntity = restTemplate.exchange(url, HttpMethod.POST, reqEntity,
          PostCsvMergeResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        result = resEntity.getBody();
      }
    } catch (Exception e) {
      log.error("postCsvMerge error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
