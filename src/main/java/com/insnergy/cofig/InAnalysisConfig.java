package com.insnergy.cofig;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Validated
@ConfigurationProperties(prefix = InAnalysisConfig.CONFIG_PREFIX)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class InAnalysisConfig {
  
  static final String CONFIG_PREFIX = "in-analysis";
  
  @NotNull
  private String csvFileUploadDirectory;
  
  @NotNull
  private String pythonServerUrl;
  
  @NotNull
  private String rServerUrl;
  
  @PostConstruct
  public void initLogConfig() {
    log.info("ConfigPrefix[{}]: {}", CONFIG_PREFIX, this);
  }
  
}
