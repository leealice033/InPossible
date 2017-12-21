package com.inpossible.service.rest;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MachineLearningService {
  private static final String PATH = "/predict";
  private static final String PYTHON = "http://127.0.0.1:8000";
  private static final String ICAN = "";//
  private final RestTemplate restTemplate;
  
  public MachineLearningService(@NonNull RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class GetDoRegressionOutput {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    List<PredictResult> predictOutput;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  private static class GetDoRegressionResponse {
    @NonNull
    String status;
    
    @NonNull
    String description;
    
    List<PredictResult> predict;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PredictResult {
    @NonNull
    String algorithm;
    @NonNull
    String price;
    @NonNull
    String situation;
    @NonNull
    String accuracy;
  }
  
  public Optional<GetDoRegressionOutput> doRegressionPredict(String coin) {
    GetDoRegressionOutput result = null;
    try {
      final String serverUrl = PYTHON + PATH + "/" + coin;
      log.debug("doMachineLearning serverurl ={}", serverUrl);
      
      final ResponseEntity<GetDoRegressionResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
          HttpEntity.EMPTY, GetDoRegressionResponse.class);
      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
        final GetDoRegressionResponse res = resEntity.getBody();
        final List<PredictResult> predictOutputList = res.getPredict()
                                                         .stream()
                                                         .map(output -> {
                                                           return PredictResult.builder()
                                                                               .algorithm(output.getAlgorithm())
                                                                               .price(output.getPrice())
                                                                               .situation(output.getSituation())
                                                                               .accuracy(output.getAccuracy())
                                                                               .build();
                                                         })
                                                         .collect(Collectors.toList());
        
        result = GetDoRegressionOutput.builder()
                                      .status(res.getStatus())
                                      .description(res.getDescription())
                                      .predictOutput(predictOutputList)
                                      .build();
        
        log.debug("doRegressionPredict result={}", result);
      }
      
    } catch (Exception e) {
      log.error("doRegressionPredict error: {}", ExceptionUtils.getMessage(e), e);
      
    }
    return Optional.ofNullable(result);
  }
  
}
