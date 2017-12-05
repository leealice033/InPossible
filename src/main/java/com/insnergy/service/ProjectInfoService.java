package com.insnergy.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.insnergy.repo.ProjectEntityRepo;
import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.ModelService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.web.ProjectManagementController;
import com.insnergy.web.ProjectManagementController.CreateProjectParam;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectInfoService {
  
  private final UserInfoService userService;
  private final CsvService csvService;
  private final ModelService modelService;
  private final ProjectEntityRepo projectRepo;
  
  public ProjectInfoService(UserInfoService userService, CsvService csvService, ProjectEntityRepo projectRepo,
      ModelService modelService) {
    this.userService = userService;
    this.csvService = csvService;
    this.modelService = modelService;
    this.projectRepo = projectRepo;
  }
  
  public Optional<ProjectInfo> buildProjectInfo(@NonNull CreateProjectParam param) {
    log.debug("CreateProjectParam param={}",param);
    ProjectInfo result = null;
    try {
      String projectId = UUID.randomUUID()
                             .toString();
      log.debug("After_UUID_projectId={}", projectId);
      //FIXME
      result = ProjectInfo.builder()
                          .id(projectId)
                          .name(param.getProjectName())
                          .deleteUrl(ProjectManagementController.getDeleteProjectPath(projectId))
                          .type(param.getProjectType())
                          .csvs(Collections.emptyList())
                          .models(Collections.emptyList())
                          .build();
      
    } catch (Exception e) {
      log.error("buildProjectInfo error: {}", ExceptionUtils.getMessage(e), e);
    }
    return Optional.ofNullable(result);
  }
  
  public void addProject(@NonNull String userId, @NonNull ProjectInfo projectInfo) {
    userService.addProject(userId, projectInfo);
    log.debug("After_DB_CREATE_PROJECT_return void");
  }
  
  public void deleteProject(@NonNull String userId, @NonNull String projectId) {
    log.debug("SERVICE_deleteProject projectId={}", projectId);
    userService.findUserInfoById(userId)
               .ifPresent(user -> {
                 
                 List<CsvInfo> csvList = userService.getUserProjectCsvList(userId, projectId);
                 List<ModelInfo> modelList = userService.getUserProjectModelList(userId, projectId);
                 ProjectInfo project = user.getProjects()
                                           .get(projectId);
                 for (CsvInfo csv : csvList) {
                   log.debug("deleting csv={}", csv);
                   if (csvService.deleteCsv(AnalysisServer.PYTHON, csv.getFileId()) != null) {
                     project.getCsvs()
                            .remove(csv.getFileId());
                   }
                 }
                 for (ModelInfo models : modelList) {
                   log.debug("deleting model={}", models);
                   if (modelService.deleteModel(AnalysisServer.PYTHON, models.getModelId()) != null) {
                     project.getModels()
                            .remove(models.getModelId());
                   }
                 }
                 
                 projectRepo.deleteById(project.getIndex());
                 user.getProjects()
                     .remove(projectId);
                 
               });
  }
  

}
