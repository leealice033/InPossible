package com.insnergy.vo.builder;

import java.util.Collections;

import com.insnergy.domain.CsvEntity;
import com.insnergy.vo.CsvInfo;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class CsvInfoBuilder {
  
  public static CsvInfo build(CsvEntity entity) {
    log.debug("input={}", entity);
    /*
     * return ProjectInfo.builder() .index(entity.getProjectIndex())
     * .deleteUrl(ProjectManagementController.getDeleteProjectPath(entity.getId(
     * ))) .id(entity.getId()) .name(entity.getName()) .type(entity.getType())
     * .csvs(Collections.emptyList()) .models(Collections.emptyList()) .build();
     */
    // FIXME
    return CsvInfo.builder()
                  .index(entity.getCsvIndex())
                  .fileId(entity.getId())
                  .stage(entity.getStage())
                  .label(entity.getLabel())
                  .build();
  }
  
}
