package com.insnergy.service.rest.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.CsvService.PostCsvInput;
import com.insnergy.service.rest.CsvService.PostCsvOutput;
import com.insnergy.service.rest.UserCsvService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.ProjectType;
import com.insnergy.util.Stages;

import lombok.extern.slf4j.Slf4j;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Slf4j
public class PythonServerIntegrationTest {
  
  @Autowired
  private CsvService csvService;
  
  @Autowired
  private UserCsvService userCsvService;
  
  private static final String TEST_USER_ID = "iii";
  private static final String TEST_PROJECT_ID = "test";
  
  @Before
  public void setup() {
    HttpServletRequest mockRequest = new MockHttpServletRequest();
    ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(mockRequest);
    RequestContextHolder.setRequestAttributes(servletRequestAttributes);
  }
  
  @After
  public void teardown() {
    RequestContextHolder.resetRequestAttributes();
  }
  
  @Test
  public void test_csv_upload_crud() throws Exception {
    cleanTestProjectCsvList();
    
    // create
    PostCsvInput input = PostCsvInput.builder()
                                     .server(AnalysisServer.PYTHON)
                                     .csvFileName("winequality-red.csv")
                                     .userId(TEST_USER_ID)
                                     .projectId(TEST_PROJECT_ID)
                                     .projectType(ProjectType.REGRESSION)
                                     .stage(Stages.DATA_PREPROCESS)
                                     .label("quality")
                                     .build();
    Optional<PostCsvOutput> _output = csvService.postCsv(input);
    assertThat(_output).isPresent();
    
    PostCsvOutput output = null;
    if (_output.isPresent()) {
      output = _output.get();
    }
    
    assertThat(output).isNotNull();
    assertThat(output.getStatus()).isEqualTo("ok");
    assertThat(output.getDescription()).isNotBlank();
    assertThat(output.getFileId()).isNotBlank();
    
    cleanTestProjectCsvList();
  }
  
  private void cleanTestProjectCsvList() {
    log.info("cleanTestProjectCsvList");
    
    userCsvService.getUserProjectCsv(AnalysisServer.PYTHON, TEST_USER_ID, TEST_PROJECT_ID)
                  .ifPresent(output -> {
                    // assertThat(output.getStatus()).isEqualTo("ok");
                    assertThat(output.getDescription()).isNotBlank();
                    
                    output.getCsvList()
                          .forEach(csv -> csvService.deleteCsv(AnalysisServer.PYTHON, csv.getFileId()));
                  });
    
    userCsvService.getUserProjectCsv(AnalysisServer.PYTHON, TEST_USER_ID, TEST_PROJECT_ID)
                  .ifPresent(output -> {
                    // assertThat(output.getStatus()).isEqualTo("ok");
                    assertThat(output.getDescription()).isNotBlank();
                    assertThat(output.getCsvList()).isEmpty();
                  });
  }
  
}
