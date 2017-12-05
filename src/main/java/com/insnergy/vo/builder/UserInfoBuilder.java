package com.insnergy.vo.builder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.insnergy.domain.UserEntity;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class UserInfoBuilder {
  
  public static UserInfo build(UserEntity entity) {
    log.debug("input={}", entity);
    
    final UserInfo.UserInfoBuilder builder = UserInfo.builder();
    
    builder.index(entity.getUserIndex())
           .id(entity.getId())
           .name(entity.getName())
           .email(entity.getEmail())
           .password(entity.getPassword());
    
    Arrays.asList(entity.getRoles()
                        .split(","))
          .forEach(builder::role);
    
    // build project map
    Map<String, ProjectInfo> projectMap = entity.getProjects()
                                                .stream()
                                                .map(ProjectInfoBuilder::build)
                                                .collect(Collectors.toMap(ProjectInfo::getId, info -> info));
    builder.projects(projectMap);
    
    // build api map
    Map<String, ApiInfo> apiMap = entity.getApis()
                                        .stream()
                                        .map(ApiInfoBuilder::build)
                                        .collect(Collectors.toMap(ApiInfo::getApiId, info -> info));
    builder.apis(apiMap);
    
    UserInfo result = builder.build();
    log.debug("build={}", result);
    return result;
  }
  
}
