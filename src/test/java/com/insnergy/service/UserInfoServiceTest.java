package com.insnergy.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserInfoServiceTest {
  
  @Autowired
  private UserInfoService service;
  
  @Test
  public void testFindUserInfoById() throws Exception {
    log.info("testFindUserInfoById");
    assertThat(service.findUserInfoById("iii")).isPresent();
  }
  
}
