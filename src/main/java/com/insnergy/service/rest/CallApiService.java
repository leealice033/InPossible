package com.insnergy.service.rest;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.insnergy.cofig.InAnalysisConfig;

import lombok.extern.slf4j.Slf4j;

//TODO
@Service
@Slf4j
public class CallApiService {
  private static final String CALL_API_PATH = "api-service";/// /api-service/{api-id}
  
  private final InAnalysisConfig config;
  private final RestTemplate restTemplate;
  
  public CallApiService(InAnalysisConfig config, RestTemplate restTemplate) {
    this.config = config;
    this.restTemplate = restTemplate;
  }
  
  
}
