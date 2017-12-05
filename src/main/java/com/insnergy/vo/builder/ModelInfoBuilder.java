package com.insnergy.vo.builder;

import com.insnergy.domain.ModelEntity;
import com.insnergy.vo.ModelInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ModelInfoBuilder {
  /*
   * private String id; private String name; private String method;
   */
  public static ModelInfo build(ModelEntity entity) {
    log.debug("input={}", entity);
    
    // FIXME
    return ModelInfo.builder()
                    .index(entity.getModelIndex())
                    .projectId(entity.getId())
                    .modelName(entity.getName())
                    .modelMethod(entity.getMethod())
                    .build();
  }
  
}
