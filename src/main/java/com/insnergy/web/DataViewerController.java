package com.insnergy.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insnergy.service.UserInfoService;
import com.insnergy.service.rest.DataViewerService;
import com.insnergy.service.rest.DataViewerService.DataInfoOutput;

import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.CsvInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DataViewerController {
  private static final String PATH = "/data-viewer";
  private static final String CHART_PATH = "/show-chart";
  private final UserInfoService userService;
  private final DataViewerService dataViewerService;
  public String imagePathForPreview;
  
  public DataViewerController(UserInfoService userService, DataViewerService dataViewerService) {
    this.userService = userService;
    this.dataViewerService = dataViewerService;
    this.imagePathForPreview = null;
  }
  
  @ModelAttribute("sidebarActiveId")
  public String populateSidebarActiveId() {
    return "sidebar-data-preprocessing";
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DataInfoPreview {
    @NonNull
    Integer index;
    @NonNull
    String columnName;
    @NonNull
    String firstValue;
    @NonNull
    String valueType;
    @NonNull
    String mean;
    @NonNull
    String standardDeviation;
    @NonNull
    String imgGetPath;
    
  }
  
  @GetMapping("/{projectId:.+}" + PATH + "/{fileId:.+}")
  public String toDataViewerPage(@PathVariable String projectId, @PathVariable String fileId, Principal principal,
      Model model, RedirectAttributes redirectAttributes) {
    String userId = principal.getName();
    log.debug("{}[action=toDataViewerPage] userId={}, projectId={}, fileId={}", PATH, userId, projectId, fileId);
    
    if (fileId == null) {
      redirectAttributes.addFlashAttribute("dataPreprocessMessage", "error: fileId is null");
      return "redirect:" + ProjectController.PATH + "/" + projectId;
    } else {
      userService.getUserProjectCsv(userId, projectId, fileId)
                 .ifPresent(csvInfo -> {
                   CsvInfo cloneInfo = SerializationUtils.clone(csvInfo);
                   log.debug("clonInfo={}", cloneInfo);
                   model.addAttribute("csvInfo", cloneInfo);
                   List<DataInfoPreview> dataPreviewList = new ArrayList<>();
                   dataViewerService.getDataViewerByFileId(cloneInfo.getServer(), fileId)
                                    .ifPresent(dataViewer -> {
                                      log.debug("dataViewerResult={}", dataViewer);
                                      log.debug("dataImagPath={}", dataViewer.getDataInfo()
                                                                             .get(0)
                                                                             .getChartPath());
                                      
                                      List<DataInfoOutput> dataOutput = dataViewer.getDataInfo();
                                      model.addAttribute("dataInfoList", dataOutput);
                                      
                                      for (Integer i = 0; i < dataOutput.size(); i++) {
                                        
                                        DataInfoPreview temp = DataInfoPreview.builder()
                                                                              .index(i)
                                                                              .columnName(dataOutput.get(i)
                                                                                                    .getColumnName())
                                                                              .firstValue(dataOutput.get(i)
                                                                                                    .getFirstValue())
                                                                              .mean(dataOutput.get(i)
                                                                                              .getMean())
                                                                              .valueType(dataOutput.get(i)
                                                                                                   .getValueType())
                                                                              .standardDeviation(dataOutput.get(i)
                                                                                                           .getStandardDeviation())
                                                                              .imgGetPath(dataOutput.get(i)
                                                                                                    .getChartPath())
                                                                              .build();
                                        dataPreviewList.add(temp);
                                        log.debug("temp_dataPreviewInfo = {}", temp);
                                        log.debug("temp_index={}", temp.getIndex());
                                        log.debug("temp__imgUrl = {}", temp.getImgGetPath());
                                      }
                                      
                                      model.addAttribute("dataPreviewInfoList", dataPreviewList);
                                    });
                 });
    }
    return "data-viewer";
  }
  
  @PostMapping(path = PATH, params = "action=Cancel")
  public String cancel(String projectId, Model model) {
    log.debug("{}[action=Cancel]", PATH);
    return "redirect:" + ProjectController.PATH + "/" + projectId;
  }
  
  @GetMapping(PATH)
  public String toShowChart(String projectId, String fileId, String imgGetPath, Model model) {
    log.debug("{}[ImageChartPath={},fileId={}]", PATH, imgGetPath, fileId);
    if (dataViewerService.getDataChartByPath(AnalysisServer.PYTHON, imgGetPath)
                         .isPresent()) {
      String getImgUrl = dataViewerService.getDataChartByPath(AnalysisServer.PYTHON, imgGetPath)
                                          .get()
                                          .getImagePath();
      log.debug("getImgUrl={}", getImgUrl);
      model.addAttribute("imgUrl", getImgUrl);
      model.addAttribute("fileId", fileId);
      model.addAttribute("projectId", projectId);
    } else {
      log.error("getDataChartByPathError!");
    }
    return "show-chart";
  }
  
  @PostMapping(path = CHART_PATH, params = "action=Back")
  public String back(String fileId, String projectId, Principal principal, Model model,
      RedirectAttributes redirectAttributes) {
    model.addAttribute("fileId", fileId);
    model.addAttribute("projectId", projectId);
    log.debug("{}[action=Back]", PATH);
    return toDataViewerPage(projectId, fileId, principal, model, redirectAttributes);
  }
  
  // TODO Download image
  @PostMapping(path = CHART_PATH, params = "action=Download")
  public String saveImage(String fileId, String projectId, String imageUrl, Principal principal, Model model,
      RedirectAttributes redirectAttributes) throws IOException {
    model.addAttribute("fileId", fileId);
    model.addAttribute("projectId", projectId);
    model.addAttribute("imageUrl={}", imageUrl);
    log.debug("{}[action=Download]", PATH);
    saveImage(imageUrl, "image.jpg");
    return toDataViewerPage(projectId, fileId, principal, model, redirectAttributes);
  }
  
  private static void saveImage(String imageUrl, String destinationFile) throws IOException {
    URL url = new URL(imageUrl);
    InputStream is = url.openStream();
    OutputStream os = new FileOutputStream(destinationFile);
    
    byte[] b = new byte[2048];
    int length;
    
    while ((length = is.read(b)) != -1) {
      os.write(b, 0, length);
    }
    
    is.close();
    os.close();
  }
}
