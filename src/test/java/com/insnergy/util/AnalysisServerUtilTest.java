package com.insnergy.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.insnergy.cofig.InAnalysisConfig;

public class AnalysisServerUtilTest {
  
  @Test
  public void testTestServerAvailable() throws Exception {
    InAnalysisConfig config = InAnalysisConfig.builder()
                                              .rServerUrl("disable")
                                              .build();
    assertThat(AnalysisServerUtil.testServerAvailable(config, AnalysisServer.R)).isFalse();
  }
  
}
