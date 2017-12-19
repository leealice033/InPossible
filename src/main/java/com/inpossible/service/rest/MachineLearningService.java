//package com.inpossible.service.rest;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;
//import com.inpossible.service.rest.MovingAverageService.PostDoMaResponse;
//import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput.PostDoMaOutputBuilder;
//import com.inpossible.service.rest.MovingAverageService.PostDoMaResponse.PostDoMaResponseBuilder;
//
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@Slf4j
//public class MachineLearningService {
//  private static final String PATH = "/ml";
//  private static final String PYTHON = "http://127.0.0.1:8000";
//  private final RestTemplate restTemplate;
//  
//  public MachineLearningService(@NonNull RestTemplate restTemplate) {
//    this.restTemplate = restTemplate;
//  }
//  
//
//  @Data
//  @AllArgsConstructor
//  @NoArgsConstructor
//  @Builder
//  public static class GetDoMlOutput {
//    @NonNull
//    String status;
//    
//    @NonNull
//    String description;
//    
//    String imagePath;
//  }
//  
//  @Data
//  @AllArgsConstructor
//  @NoArgsConstructor
//  @Builder
//  public static class GetDoMlResponse {
//    @NonNull
//    String status;
//    
//    @NonNull
//    String description;
//    
//    String image_path;
//  }
//  
////  public Optional<GetDoMlOutput> doMachineLearning() {
////    GetDoMlOutput result = null;
////    try {
////      final String serverUrl = PYTHON + PATH;
////      log.debug("doMachineLearning serverurl ={}", serverUrl);
////      
////      final ResponseEntity<GetDoMlResponse> resEntity = restTemplate.exchange(serverUrl, HttpMethod.GET,
////          HttpEntity.EMPTY, GetDoMlResponse.class);
////      if (HttpStatus.OK.equals(resEntity.getStatusCode()) && resEntity.hasBody()) {
////        final GetDoMlResponse res = resEntity.getBody();
////        final List<GetDoMlOutput> apiListForOutput = res.getApi_list()
////                                                           .stream()
////                                                           .map(this::toApiListOutput)
////                                                           .collect(Collectors.toList());
////        
////        result = GetDoMlOutput.builder()
////                             .status(res.getStatus())
////                             .description(res.getDescription())
////                             .build();
////        
////        log.debug("getApiById result={}", result);
////      }
////      
////    } catch (Exception e) {
////      log.error("getApi error: {}", ExceptionUtils.getMessage(e), e);
////      
////    }
////    return Optional.ofNullable(result);
////  }
//  
//}
