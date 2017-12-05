package com.insnergy.domain.builder;

import com.insnergy.domain.ModelEntity;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ProjectInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ModelEntityBuilder {
  
  public static ModelEntity build(ModelInfo info) {
    log.debug("input={}", info);
    final ModelEntity.ModelEntityBuilder modelBuilder = ModelEntity.builder();
    modelBuilder.modelIndex(info.getIndex())
                .id(info.getModelId())
                .name(info.getModelName())
                .method(info.getModelMethod());
    
    final ModelEntity result = modelBuilder.build();
    
    return result;
  }
  
  public static ModelEntity build(ModelInfo info, ProjectInfo ownerProjectInfo) {
    log.debug("input=model={},ownerProject{},", info, ownerProjectInfo);
    
    final ModelEntity result = build(info);
    result.setOwnerProject(ProjectEntityBuilder.build(ownerProjectInfo));
    
    log.debug("result={}", result);
    return result;
  }
  
}
