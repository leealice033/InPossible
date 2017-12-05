package com.insnergy.service;

import org.springframework.stereotype.Service;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.domain.ModelEntity;
import com.insnergy.domain.ProjectEntity;
import com.insnergy.domain.builder.ModelEntityBuilder;
import com.insnergy.repo.ModelEntityRepo;
import com.insnergy.repo.ProjectEntityRepo;
import com.insnergy.service.rest.ModelService;
import com.insnergy.service.rest.ModelService.ModelListOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.web.ModelManagementController;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelInfoService {
  private final InAnalysisConfig config;
  private final UserInfoService userService;
  private final ModelService modelService;
  private final ModelEntityRepo modelRepo;
  private final ProjectEntityRepo projectRepo;
  
  public ModelInfoService(InAnalysisConfig config, UserInfoService userService, ModelService modelService,
      ModelEntityRepo modelRepo, ProjectEntityRepo projectRepo) {
    this.config = config;
    this.userService = userService;
    this.modelService = modelService;
    this.modelRepo = modelRepo;
    this.projectRepo = projectRepo;
  }
  
  // TODO add ModelOutput to Memory (index = null_)
  public Boolean buildModelInfoStoreToDB(final String modelId) {
    Boolean result = false;
    if (modelService.getModel(AnalysisServer.PYTHON, modelId)
                    .isPresent()) {
      ModelListOutput modelListOutput = modelService.getModel(AnalysisServer.PYTHON, modelId)
                                                    .get()
                                                    .getModelList()
                                                    .get(0);
      
      ModelInfo modelInfo = ModelInfo.builder()
                                     .server(AnalysisServer.PYTHON)
                                     .userId(modelListOutput.getUserId())
                                     .modelId(modelListOutput.getModelId())
                                     .modelName(modelListOutput.getModelName())
                                     .modelMethod(modelListOutput.getModelMethod())
                                     .deleteUrl(ModelManagementController.getDeleteModelPath(
                                         modelListOutput.getProjectId(), modelListOutput.getModelId()))
                                     .projectId(modelListOutput.getProjectId())
                                     .projectType(modelListOutput.getProjectType())
                                     .label(modelListOutput.getLabel())
                                     .build();
      
      log.debug("modelInfo={}", modelInfo);
      if (addModelToDB(modelInfo)) {
        result = true;
      }
      
    }
    return result;
  }
  
  // TODO add ModelInfo to DB (entity index != null_) and set Model info Index
  public Boolean addModelToDB(ModelInfo model) {
    Boolean result = false;
    if (projectRepo.findOneById(model.getProjectId())
                   .isPresent()) {
      ProjectEntity projectEntity = projectRepo.findOneById(model.getProjectId())
                                               .get();
      log.debug("find projectEntity={}", projectEntity);
      ProjectInfo projectInfo = userService.getUserProject(model.getUserId(), model.getProjectId())
                                           .get();
      
      ModelEntity modelEntity = ModelEntityBuilder.build(model);
      modelEntity.setOwnerProject(projectEntity);
      
      ModelEntity savedModelEntity = modelRepo.save(modelEntity);
      log.debug("add model_ Save modelEntity={}", savedModelEntity);
      
      model.setIndex(savedModelEntity.getModelIndex());
      log.debug("modelInfo index set ={}", model.getIndex());
      
      if (model.getIndex() != null) {
        result = true;
      }
      projectInfo.getModels()
                 .add(model);
      
    }
    return result;
  }
  
  public Long getDbIndex(String modelId) {
    Long result = null;
    if (modelRepo.findOneById(modelId)
                 .isPresent()) {
      ModelEntity modelEntity = modelRepo.findOneById(modelId)
                                         .get();
      log.debug("modelEntity={}", modelEntity);
      result = modelEntity.getModelIndex();
    }
    return result;
  }
  
}
