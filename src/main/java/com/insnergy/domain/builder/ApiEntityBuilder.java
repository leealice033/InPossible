package com.insnergy.domain.builder;

import com.insnergy.domain.ApiEntity;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.UserInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ApiEntityBuilder {
  
  public static ApiEntity build(ApiInfo info) {
    log.debug("ApiEntity build_input={}", info);
    final ApiEntity result = ApiEntity.builder()
                                      .apiIndex(info.getIndex())
                                      .id(info.getApiId())
                                      .name(info.getApiName())
                                      .description(info.getApiDescription())
                                      .path(info.getApiPath())
                                      .build();
    log.debug("ApiEntity build result={}", result);
    return result;
  }
  
  public static ApiEntity build(ApiInfo info, UserInfo ownerUserInfo) {
    log.debug("input={}", info);
    final ApiEntity result = build(info);
    result.setOwnerUser(UserEntityBuilder.build(ownerUserInfo));
    log.debug("ApiEntity build result _user_info_result={}", result);
    return result;
  }
  
}
