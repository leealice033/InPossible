package com.insnergy.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.insnergy.domain.ApiEntity;
import com.insnergy.domain.ProjectEntity;
import com.insnergy.domain.UserEntity;
import com.insnergy.domain.builder.ApiEntityBuilder;
import com.insnergy.domain.builder.ProjectEntityBuilder;
import com.insnergy.domain.builder.UserEntityBuilder;
import com.insnergy.repo.ApiEntityRepo;
import com.insnergy.repo.ProjectEntityRepo;
import com.insnergy.repo.UserEntityRepo;
import com.insnergy.service.rest.UserApiService;
import com.insnergy.service.rest.UserCsvService;
import com.insnergy.service.rest.UserModelService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ModelInfo;
import com.insnergy.vo.ProjectInfo;
import com.insnergy.vo.UserInfo;
import com.insnergy.vo.builder.UserInfoBuilder;
import com.insnergy.web.RegistrationController.CreateAccountParam;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserInfoService {
  
  // key=UserInfo.id
  private final Map<String, UserInfo> userMap;
  
  private final UserCsvService userCsvService;
  private final UserModelService userModelService;
  private final UserApiService userApiService;
  
  private final UserEntityRepo userRepo;
  private final ProjectEntityRepo projectRepo;
  private final ApiEntityRepo apiRepo;
  
  public UserInfoService(UserCsvService userCsvService, UserModelService userModelService,
      UserApiService userApiService, UserEntityRepo userRepo, ProjectEntityRepo projectRepo, ApiEntityRepo apiRepo) {
    this.userMap = new ConcurrentHashMap<>();
    this.userCsvService = userCsvService;
    this.userModelService = userModelService;
    this.userApiService = userApiService;
    this.userRepo = userRepo;
    this.projectRepo = projectRepo;
    this.apiRepo = apiRepo;
    
  }
  
  @PostConstruct
  public void initUserMapByLoadDatabase() {
    if (userRepo.count() == 0) {
      addDefaultUsersToDatabase();
    }
    
    userRepo.findAll()
            .stream()
            .map(UserInfoBuilder::build)
            .forEach(userInfo -> userMap.put(userInfo.getId(), userInfo));
  }
  
  public static List<String> DEFAULT_ADMIN_USERS = Arrays.asList("iii", "teddy", "swanky", "mark", "alice", "coco");
  public static List<String> DEFAULT_USERS = Arrays.asList("guest");
  
  private void addDefaultUsersToDatabase() {
    log.info("DB is empty, init DB with ADMIN_USERS:{} and USERS:{}", DEFAULT_ADMIN_USERS, DEFAULT_USERS);
    
    DEFAULT_ADMIN_USERS.forEach(userId -> {
      UserEntity adminUser = UserEntity.builder()
                                       .id(userId)
                                       .name(userId)
                                       .email("inanalysis.github.io@gmail.com")
                                       .password(userId)
                                       .roles("ADMIN,USER")
                                       .build();
      userRepo.save(adminUser);
    });
    
    DEFAULT_USERS.forEach(userId -> {
      UserEntity user = UserEntity.builder()
                                  .id(userId)
                                  .name(userId)
                                  .email("inanalysis.github.io@gmail.com")
                                  .password(userId)
                                  .roles("USER")
                                  .build();
      userRepo.save(user);
    });
  }
  
  public List<UserInfo> findAll() {
    return userMap.values()
                  .stream()
                  .collect(Collectors.toList());
  }
  
  public Optional<UserInfo> findUserInfoById(String userId) {
    final Optional<UserInfo> result = Optional.ofNullable(userMap.get(userId));
    log.debug("findUserInfoById[{}]={}", userId, result);
    return result;
  }
  
  public void refreshUserProject(String userId, String projectId) {
    findUserInfoById(userId).ifPresent(userInfo -> {
      final ProjectInfo project = userInfo.getProjects()
                                          .get(projectId);
      
      userCsvService.getUserProjectCsv(AnalysisServer.PYTHON, userId, project.getId())
                    .ifPresent(output -> project.setCsvs(output.getCsvList()));
      
      userModelService.getUserProjectModel(AnalysisServer.PYTHON, userId, project.getId())
                      .ifPresent(output -> project.setModels(output.getModelList()));
    });
  }
  
  public void refreshUserApi(String userId) {
    findUserInfoById(userId).ifPresent(userInfo -> {
      
      userApiService.getUserApi(AnalysisServer.PYTHON, userId)
                    .ifPresent(output -> userInfo.setApis(output.getApiList()
                                                                .stream()
                                                                .collect(Collectors.toMap(ApiInfo::getApiId,
                                                                    apiInfo -> apiInfo))));
    });
  }
  
  public List<CsvInfo> getUserProjectCsvList(String userId, String projectId) {
    List<CsvInfo> result = null;
    
    Optional<UserInfo> _user = findUserInfoById(userId);
    if (_user.isPresent()) {
      UserInfo userInfo = _user.get();
      ProjectInfo project = userInfo.getProjects()
                                    .get(projectId);
      if (project != null) {
        result = project.getCsvs();
      }
    }
    //FIXME
    if (result == null) {
      result = Collections.emptyList();
    }
    log.debug("getUserProjectCsvList[{}:{}]={}", userId, projectId);
    return result;
  }
  
  public Optional<CsvInfo> getUserProjectCsv(String userId, String projectId, String fileId) {
    return getUserProjectCsvList(userId, projectId).stream()
                                                   .filter(csvInfo -> StringUtils.equals(csvInfo.getFileId(), fileId))
                                                   .findAny();
  }
  
  public Optional<ProjectInfo> getUserProject(String userId, String projectId) {
    ProjectInfo result = null;
    Optional<UserInfo> _user = findUserInfoById(userId);
    if (_user.isPresent()) {
      UserInfo userInfo = _user.get();
      ProjectInfo project = userInfo.getProjects()
                                    .get(projectId);
      if (project != null) {
        result = project;
      }
    }
    return Optional.ofNullable(result);
    
  }
  
  public List<ModelInfo> getUserProjectModelList(String userId, String projectId) {
    List<ModelInfo> result = null;
    
    Optional<UserInfo> _user = findUserInfoById(userId);
    if (_user.isPresent()) {
      UserInfo userInfo = _user.get();
      ProjectInfo project = userInfo.getProjects()
                                    .get(projectId);
      if (project != null) {
        result = project.getModels();
      }
    }
    //FIXME
    if (result == null) {
      result = Collections.emptyList();
    }
    log.debug("getUserProjectModelList[{}:{}]={}", userId, projectId);
    return result;
  }
  
  public Optional<ModelInfo> getUserProjectModel(String userId, String projectId, String modelId) {
    return getUserProjectModelList(userId, projectId).stream()
                                                     .filter(modelInfo -> StringUtils.equals(modelInfo.getModelId(),
                                                         modelId))
                                                     .findAny();
  }
  
  public List<ApiInfo> getUserApiList(String userId) {
    List<ApiInfo> result = null;
    Optional<UserInfo> _user = findUserInfoById(userId);
    if (_user.isPresent()) {
      UserInfo userInfo = _user.get();
      result = userInfo.getApis()
                       .values()
                       .stream()
                       .collect(Collectors.toList());
    }
    
    return result;
  }
  
  public Optional<ApiInfo> getUserApiInfo(String userId, String projectId, String apiId) {
    return getUserApiList(userId).stream()
                                 .filter(apiInfo -> StringUtils.equals(apiInfo.getApiId(), apiId))
                                 .findAny();
  }
  
  public void addUser(@NonNull CreateAccountParam param) {
    log.debug("addUser CreateAccountParam={}", param);
    UserInfo userInfo = UserInfo.builder()
                                .id(param.getStudentId())
                                .name(param.getUserName())
                                .email(param.getEmail())
                                .role("USER")
                                .password(param.getPassword())
                                .projects(new HashMap<String, ProjectInfo>())
                                .apis(new HashMap<String, ApiInfo>())
                                .build();
    
    UserEntity userEntity = UserEntityBuilder.build(userInfo);
    
    UserEntity savedUserEntity = userRepo.save(userEntity);
    
    userInfo.setIndex(savedUserEntity.getUserIndex());
    log.debug("AfterSetIndexFromDB_UserInfo={}", userInfo);
    userMap.put(userInfo.getId(), userInfo);
  }
  
  public void addProject(@NonNull String userId, ProjectInfo projectInfo) {
    log.debug("addProject ProjectInfo={}", projectInfo);
    findUserInfoById(userId).ifPresent(userInfo -> {
      log.debug("addProject to userInfo={}", userInfo);
      
      UserEntity user = UserEntityBuilder.build(userInfo);
      
      ProjectEntity projectEntity = ProjectEntityBuilder.build(projectInfo);
      
      projectEntity.setOwnerUser(user);
      
      ProjectEntity savedProjectEntity = projectRepo.save(projectEntity);
      
      log.debug("addProject ProjectEntity={}", projectEntity);
      
      
      projectInfo.setIndex(savedProjectEntity.getProjectIndex());
      
      log.debug("AfterSetIndexFromDB_ProjectInfo={}", projectInfo);
      log.debug("add_info_id={}", projectInfo.getId());
      
      userInfo.getProjects()
              .put(projectInfo.getId(), projectInfo);
      
      log.debug("afterPutNewProject_userInfo={}", userInfo);
      
    });
  }
  
  // TODO (1) build api to DB (2) if (1) success ->add api from DB to memeory
  public void addApi(@NonNull String userId, ApiInfo apiInfo) {
    log.debug("addUser's={}Api,ApiInfo={}", userId, apiInfo);
    findUserInfoById(userId).ifPresent(userInfo -> {
      UserEntity user = userRepo.findOneById(userId)
                                .get();
      
      ApiEntity apiEntity = ApiEntityBuilder.build(apiInfo);
      apiEntity.setOwnerUser(user);
      
      ApiEntity savedApiEntity = apiRepo.save(apiEntity);
      
      log.debug("addApi ApiEntity={}", apiEntity);
      
      apiInfo.setIndex(savedApiEntity.getApiIndex());
      
      log.debug("AfterSetIndexFromDB_ApiInfo={}", apiInfo);
      
      userInfo.getApis()
              .put(apiInfo.getApiId(), apiInfo);
      
      log.debug("checkApiInfo={}", userInfo.getApis()
                                           .get(apiInfo.getApiId()));
      
    });
    findUserInfoById(userId).ifPresent(userCheckInfo -> {
      log.debug("userInfo___={}", userCheckInfo);
    });
  }
}
