package com.insnergy.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.insnergy.service.ApiInfoService;
import com.insnergy.vo.ApiInfo;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Ignore
public class MakeApiControllerTest {
  
  @Autowired
  private MakeApiController controller;
  
  @Autowired
  private ApiInfoService apiInfoService;
  
  @Ignore
  @Test
  public void testSaveApiInfoToDB() throws Exception {
    log.info("testSaveApiInfoToDB");
    
    // Given
    String userId = "iii";
    ApiInfo apiInfo = ApiInfo.builder()
                             .userId(userId)
                             .apiName("API_NAME")
                             .build();
    
    // When
    //Optional<ApiInfo> _apiInfo = controller.saveApiInfoToDB(userId, apiInfo);
    
    // Then
//    assertThat(_apiInfo).isPresent();
//    ApiInfo apiInfoResult = _apiInfo.get();
//    
//    assertThat(apiInfoResult).isEqualTo(apiInfo);
  }
  
}
