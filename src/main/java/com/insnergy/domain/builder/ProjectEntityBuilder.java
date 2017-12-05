package com.insnergy.domain.builder;

import java.util.Set;
import java.util.stream.Collectors;

import com.insnergy.domain.CsvEntity;
import com.insnergy.domain.ModelEntity;
import com.insnergy.domain.ProjectEntity;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ProjectEntityBuilder {
  
  public static ProjectEntity build(ProjectInfo info) {
    log.debug("input={}", info);
    
    final ProjectEntity.ProjectEntityBuilder projectBuilder = ProjectEntity.builder();
    projectBuilder.projectIndex(info.getIndex())
                  .id(info.getId())
                  .name(info.getName())
                  .type(info.getType());
    
    // build csvs
    Set<CsvEntity> csvs = info.getCsvs()
                              .stream()
                              .map(CsvEntityBuilder::build)
                              .collect(Collectors.toSet());
    projectBuilder.csvs(csvs);
    
    // build models
    Set<ModelEntity> models = info.getModels()
                                  .stream()
                                  .map(ModelEntityBuilder::build)
                                  .collect(Collectors.toSet());
    projectBuilder.models(models);
    
    // build and set owner project
    final ProjectEntity result = projectBuilder.build();
    if (csvs != null) {
      csvs.forEach(csv -> csv.setOwnerProject(result));
    }
    
    if (models != null) {
      models.forEach(model -> model.setOwnerProject(result));
    }
    log.debug("result={}", result);
    return result;
  }
  
  public static ProjectEntity build(ProjectInfo info, UserInfo ownerUserInfo) {
    log.debug("input={}", info);
    final ProjectEntity result = build(info);
    result.setOwnerUser(UserEntityBuilder.build(ownerUserInfo));
    log.debug("result={}", result);
    return result;
  }
  
}
