package com.insnergy.service;



import org.springframework.stereotype.Service;

import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.domain.CsvEntity;
import com.insnergy.domain.ProjectEntity;
import com.insnergy.domain.builder.CsvEntityBuilder;
import com.insnergy.repo.CsvEntityRepo;
import com.insnergy.repo.ProjectEntityRepo;
import com.insnergy.service.rest.CsvService;
import com.insnergy.service.rest.CsvService.GetCsvOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.CsvInfo;
import com.insnergy.vo.ProjectInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvInfoService {
  
  private final InAnalysisConfig config;
  private final UserInfoService userService;
  private final CsvService csvService;
  private final CsvEntityRepo csvRepo;
  private final ProjectEntityRepo projectRepo;
  
  public CsvInfoService(InAnalysisConfig config, UserInfoService userService, CsvService csvFileService,
      CsvEntityRepo csvRepo, ProjectEntityRepo projectRepo) {
    this.config = config;
    this.userService = userService;
    this.csvService = csvFileService;
    this.csvRepo = csvRepo;
    this.projectRepo = projectRepo;
  }
  
  // TODO add CsvOutput to Memory (index = null_)
  public Boolean buildCsvInfoStoreToDB(final String fileId) {
    Boolean result = false;
    if (csvService.getCsv(AnalysisServer.PYTHON, fileId)
                  .isPresent()) {
      GetCsvOutput csvOutput = csvService.getCsv(AnalysisServer.PYTHON, fileId)
                                         .get();
      CsvInfo csvFile = csvOutput.getCsvList()
                                 .get(0);
      log.debug("csvFile={}", csvFile);
      if (addFileToDB(csvFile)) {
        result = true;
      }
    }
    
    return result;
  }
  
  // TODO add CsvInfo to DB (entity index != null_) and set CsvIndex
  public Boolean addFileToDB(CsvInfo csv) {
    Boolean result = false;
    if (projectRepo.findOneById(csv.getProjectId())
                   .isPresent()) {
      ProjectEntity projectEntity = projectRepo.findOneById(csv.getProjectId())
                                               .get();
      log.debug("find projectEntity={}", projectEntity);
      ProjectInfo projectInfo = userService.getUserProject(csv.getUserId(), csv.getProjectId())
                                           .get();
      CsvEntity csvEntity = CsvEntityBuilder.build(csv);
      csvEntity.setOwnerProject(projectEntity);
      
      log.debug("addFile csvEntity={}", csvEntity);
      
      CsvEntity savedCsvEntity = csvRepo.save(csvEntity);
      log.debug("addFile Save csvEntity={}", savedCsvEntity);
      
      csv.setIndex(savedCsvEntity.getCsvIndex());
      log.debug("AfterSetIndexFromDB_CsvInfo={}", csv);
      if (csv.getIndex() != null) {
        result = true;
      }
      
      projectInfo.getCsvs()
                 .add(csv);
      
    }
    return result;
  }
  
  public Long getDbIndex(String fileId) {
    Long result = null;
    if (csvRepo.findOneById(fileId)
               .isPresent()) {
      CsvEntity csvEntity = csvRepo.findOneById(fileId)
                                   .get();
      log.debug("csvEntity={}", csvEntity);
      result = csvEntity.getCsvIndex();
    }
    return result;
  }
}
