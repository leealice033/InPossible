package com.inpossible.service.rest;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MovingAverageService {
  private static final String PATH = "/ma";
  private static final String PYTHON = "http://127.0.0.1:8000";
  private static final String ICAN = "";//
  private final RestTemplate restTemplate;
  
  public MovingAverageService(@NonNull RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostDoMaInput {
    String coin;
    String zoom;
    List<MovingAverage> ma;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class MovingAverage {
    String algorithm;
    Boolean show;
    Integer period;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class PostDoMaRequest {
    @NonNull
    String coin;
    @NonNull
    String zoom;
    @NonNull
    List<MovingAverage> ma;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostDoMaOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String imagePath;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PostDoMaResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    String image_path;
  }
  
  public Optional<PostDoMaOutput> postDoMovingAverage(@NonNull PostDoMaInput input) {
    PostDoMaOutput result = null;
    try {
      log.debug("input={}", input);
      final String serverUrl = PYTHON + PATH +"/"+ input.getCoin();
      log.debug("pythonUrl={}", serverUrl);
      final PostDoMaRequest req = PostDoMaRequest.builder()
                                                 .coin(input.getCoin())
                                                 .zoom(input.getZoom())
                                                 .ma(input.getMa())
                                                 .build();
      log.debug("PostDoMaRequest req={}", req);
      final HttpEntity<PostDoMaRequest> reqEntity = new HttpEntity<PostDoMaRequest>(req);
      final ResponseEntity<PostDoMaResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.POST, reqEntity,
          PostDoMaResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final PostDoMaResponse res = resEntity.getBody();
        result = PostDoMaOutput.builder()
                               .status(res.getStatus())
                               .description(res.getDescription())
                               .imagePath(res.getImage_path())
                               .build();
      }
    } catch (Exception e) {
      log.error("postDoMovingAverage: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
}
