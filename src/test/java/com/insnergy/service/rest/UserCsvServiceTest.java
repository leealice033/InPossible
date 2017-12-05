package com.insnergy.service.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.insnergy.service.rest.UserCsvService.GetUserCsvOutput;
import com.insnergy.util.AnalysisServer;

import lombok.extern.slf4j.Slf4j;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserCsvServiceTest {
  
  @Autowired
  private UserCsvService service;
  
  @Test
  public void test_getUserCsv_only_user() throws Exception {
    log.info("test_getUserCsv_only_user");
    final Optional<GetUserCsvOutput> _output = service.getUserCsv(AnalysisServer.PYTHON, "iii");
    assertThat(_output).isPresent();
  }
  
  @Test
  public void test_getUserProjectCsv() throws Exception {
    log.info("test_getUserProjectCsv");
    final Optional<GetUserCsvOutput> _output = service.getUserProjectCsv(AnalysisServer.PYTHON, "iii",
        "default_project_id");
    assertThat(_output).isPresent();
  }
  
  @Test
  public void test_getUserProjectCsv_with_stage() throws Exception {
    log.info("test_getUserProjectCsv_with_stage");
    final Optional<GetUserCsvOutput> _output = service.getUserProjectCsv(AnalysisServer.PYTHON, "iii",
        "default_project_id", "data-preprocess");
    assertThat(_output).isPresent();
  }
  
}
