package com.insnergy.domain.builder;

import com.insnergy.domain.CsvEntity;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ProjectInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class CsvEntityBuilder {
  
  public static CsvEntity build(CsvInfo info) {
    log.debug("input={}", info);
    
    final CsvEntity.CsvEntityBuilder csvBuilder = CsvEntity.builder();
    csvBuilder.csvIndex(info.getIndex())
              .id(info.getFileId())
              .stage(info.getStage())
              .label(info.getLabel());
    
    final CsvEntity result = csvBuilder.build();
    
    return result;
    
  }
  
  public static CsvEntity build(CsvInfo info, ProjectInfo ownerProjectInfo) {
    log.debug("input={}", info);
    final CsvEntity result = build(info);
    result.setOwnerProject(ProjectEntityBuilder.build(ownerProjectInfo));
    log.debug("result={}", result);
    return result;
  }
  
}
