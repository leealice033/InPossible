package com.insnergy.vo.builder;

import java.util.Collections;

import com.insnergy.domain.ProjectEntity;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.web.ProjectManagementController;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ProjectInfoBuilder {
  
  public static ProjectInfo build(ProjectEntity entity) {
    log.debug("input={}", entity);
    
    // FIXME
    return ProjectInfo.builder()
                      .index(entity.getProjectIndex())
                      .deleteUrl(ProjectManagementController.getDeleteProjectPath(entity.getId()))
                      .id(entity.getId())
                      .name(entity.getName())
                      .type(entity.getType())
                      .csvs(Collections.emptyList())
                      .models(Collections.emptyList())
                      .build();
  }
  
}
