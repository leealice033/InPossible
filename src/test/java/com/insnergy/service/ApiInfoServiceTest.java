package com.insnergy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.insnergy.repo.UserEntityRepo;
import com.insnergy.service.rest.ApiService;
import com.insnergy.service.rest.ApiService.GetApiListOutput;
import com.insnergy.service.rest.ApiService.GetApiOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.UserInfo;
import com.insnergy.web.ApiManagementController;

import lombok.extern.slf4j.Slf4j;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ApiInfoServiceTest {
  
  @Autowired
  private ApiInfoService service;
  
  @Autowired
  private UserInfoService userService;
  
  @Autowired
  private UserEntityRepo userRepo;
  
  @Autowired
  private ApiManagementController controller;
  
  @MockBean
  private ApiService apiService;
  
  final AnalysisServer server = AnalysisServer.PYTHON;
  final String userId = "iii";
  final String apiId = UUID.randomUUID()
                           .toString();
  final String apiName = "API_NAME";
  
  final String modelId = UUID.randomUUID()
                             .toString();
  
  @Before
  public void setUp() throws Exception {
    GetApiListOutput listOutput = GetApiListOutput.builder()
                                                  .userId(userId)
                                                  .apiId(apiId)
                                                  .apiName(apiName)
                                                  .apiDescription("mock desc")
                                                  .apiPath("mock api path")
                                                  .inputFormat(Collections.emptyList())
                                                  .outputFormat(Collections.emptyList())
                                                  .modelId(modelId)
                                                  .build();
    GetApiOutput output = GetApiOutput.builder()
                                      .status("ok")
                                      .description("mock desc")
                                      .apiList(Arrays.asList(listOutput))
                                      .build();
    when(apiService.getApiById(server, apiId)).thenReturn(Optional.of(output));
  }
  
  @Ignore
  @Transactional
  @Test
  public void testBuildApiInfoToDB() throws Exception {
    // Given
    ApiInfo apiInfo = ApiInfo.builder()
                             .userId(userId)
                             .apiId(apiId)
                             .apiName(apiName)
                             .modelId(modelId)
                             .build();
    // When
    Optional<ApiInfo> _apiInfo = service.buildApiInfoToDB(server, apiInfo);
    service.addApiToDB(userId, _apiInfo.get());
    
    // Then
    UserInfo userInfo = userService.findUserInfoById(userId)
                                   .get();
    assertThat(userInfo.getApis()).hasSize(1);
    
    ApiInfo testApiInfo = userInfo.getApis()
                                  .get(apiId);
    assertThat(testApiInfo).isNotNull();
    assertThat(testApiInfo.getIndex()).isNotNull();
    log.info("testApiInfo={}", testApiInfo);
    
    // When
    controller.deleteApiByApiId(apiId, userId);
    
    // Then
    userInfo = userService.findUserInfoById(userId)
                          .get();
    assertThat(userInfo.getApis()).hasSize(0);
  }
  
}
