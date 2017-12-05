package com.insnergy.web;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.insnergy.domain.ApiEntity;
import com.insnergy.repo.ApiEntityRepo;
import com.insnergy.service.ApiInfoService;
import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.ApiService;
import com.insnergy.service.rest.MakeApiService;
import com.insnergy.service.rest.UserApiService;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.ApiInfo;
import com.insnergy.vo.UserInfo;
import com.insnergy.web.MakeApiController.DoMakeApiParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ApiManagementController {
  
  private static final String API_PATH = "/api-management";
  private static final String DELETE_API = "/api-delete";
  
  private final UserInfoService userService;
  private final UserApiService userApiService;
  private final ApiService apiService;
  private final ApiEntityRepo apiRepo;
  private final Map<String, DoMakeApiParam> editApiInputCache;
  
  public ApiManagementController(UserInfoService userService, UserApiService userApiService, ApiService apiService,
      ApiEntityRepo apiRepo) {
    this.userService = userService;
    this.userApiService = userApiService;
    this.apiService = apiService;
    this.apiRepo = apiRepo;
    this.editApiInputCache = new ConcurrentHashMap<>();
  }
  
  @GetMapping(API_PATH)
  public String toApiManagePage(Principal principal, Model model) {
    final String userId = principal.getName();
    userService.refreshUserApi(userId);// FIXME
    log.debug("index userId={}", userId);
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      
      if (userInfo.getApis() != null && userInfo.getApis()
                                                .size() == 0) {
        
        // userApiService.getUserApi(AnalysisServer.PYTHON, userId)
        // .ifPresent(output -> {
        // userInfo.setApis(output.getApiList()
        // .stream()
        // .collect(Collectors.toMap(ApiInfo::getApiId, apiInfo -> apiInfo)));
        //
        // });
      }
      
      model.addAttribute("userId", userId);
      model.addAttribute("apis", userInfo.getApis()
                                         .values()
                                         .stream()
                                         .collect(Collectors.toList()));
      
    });
    return "api-management";
  }
  
  public static String getDeleteApiPath(String apiId) {
    log.debug("deleteMode={}", String.format("%s/%s", DELETE_API, apiId));
    return String.format("%s/%s", DELETE_API, apiId);
  }
  
  @GetMapping(DELETE_API + "/{apiId:.+}")
  public String deleteApi(@PathVariable String apiId, Principal principal) {
    log.info("[deleteApi]  apiId={}", apiId);
    final String userId = principal.getName();
    
    deleteApiByApiId(apiId, userId);
    
    return "redirect:" + API_PATH;
  }
  
  public void deleteApiByApiId(String apiId, final String userId) {
    log.debug("enter_deleteApiByApiId_apiId={},userId={}", apiId, userId);
    Optional<UserInfo> _user = userService.findUserInfoById(userId);
    _user.ifPresent(userInfo -> {
      log.debug("enter_deleteApiByApiId_apiId={},userInfo={}", apiId, userInfo);
      if (deleteApiPython(apiId)) {
        log.debug("PYTHON api deleted_id={}", apiId);
        if (deleteApiDB(apiId)) {
          log.debug("DB api deleted_id={}", apiId);
          userInfo.getApis()
                  .remove(apiId);
          userService.refreshUserApi(userId);
        }
      }
    });
  }
  
  public Boolean deleteApiPython(String apiId) {
    Boolean result = false;
    if (apiService.deleteApi(AnalysisServer.PYTHON, apiId)
                  .isPresent()) {
      
      result = true;
      log.debug("deleteApiPython={}", result);
    }
    return result;
  }
  
  public Boolean deleteApiDB(String apiId) {
    Boolean result = false;
    if (apiRepo.findOneById(apiId)
               .isPresent()) {
      ApiEntity apiEntity = apiRepo.findOneById(apiId)
                                   .get();
      log.debug("findApiEntityById{}={}", apiId, apiEntity);
      apiRepo.delete(apiEntity);
      result = true;
    } else {
      log.debug("can't find api entity by apiId={}", apiId);
    }
    return result;
  }
}
