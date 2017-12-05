package com.insnergy.domain.builder;

import java.util.Set;
import java.util.stream.Collectors;

import com.insnergy.domain.ApiEntity;
import com.insnergy.domain.ProjectEntity;
import com.insnergy.domain.UserEntity;
import com.insnergy.vo.UserInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class UserEntityBuilder {
  
  public static UserEntity build(UserInfo info) {
    log.debug("input={}", info);
    final UserEntity.UserEntityBuilder builder = UserEntity.builder();
    
    builder.userIndex(info.getIndex())
           .id(info.getId())
           .name(info.getName())
           .email(info.getEmail())
           .password(info.getPassword())
           .roles(info.getRoles()
                      .stream()
                      .collect(Collectors.joining(",")));
    
    // build projects
    final Set<ProjectEntity> projects = info.getProjects()
                                            .values()
                                            .stream()
                                            .map(ProjectEntityBuilder::build)
                                            .collect(Collectors.toSet());
    builder.projects(projects);
    
    // build apis
    final Set<ApiEntity> apis = info.getApis()
                                    .values()
                                    .stream()
                                    .map(ApiEntityBuilder::build)
                                    .collect(Collectors.toSet());
    builder.apis(apis);
    
    // build and set owner user
    final UserEntity result = builder.build();
    
    projects.forEach(project -> project.setOwnerUser(result));
    apis.forEach(api -> api.setOwnerUser(result));
    
    log.debug("result={}", result);
    return result;
  }
  
}
