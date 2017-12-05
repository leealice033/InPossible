package com.insnergy.web.admin;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.insnergy.service.LogFileService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/logs")
@Slf4j
public class LogFileController {
  
  private static final String DEFAULT_LIST_ALL = "all";
  
  @Autowired
  private LogFileService service;
  
  @GetMapping
  public String listAllLogs(Model model) {
    return listFilterLogs(DEFAULT_LIST_ALL, model);
  }
  
  @GetMapping(path = "/{filter_name:.+}")
  public String listFilterLogs(@PathVariable("filter_name") String filterName, Model model) {
    log.debug("[listFilterLogs] filterName: {}", filterName);
    
    if (StringUtils.equalsIgnoreCase(DEFAULT_LIST_ALL, filterName)) {
      model.addAttribute("logs", service.getLogFiles());
    } else {
      model.addAttribute("logs", service.getLogFiles(filterName));
    }
    return "logs";
  }
  
  @GetMapping(path = "/file/{file_name:.+}")
  public ResponseEntity<byte[]> downloadLogFile(@PathVariable("file_name") String fileName, Model model) {
    log.debug("[downloadLogFile] fileName: {}", fileName);
    
    Optional<byte[]> _fileBytes = service.readLogFile(fileName);
    if (_fileBytes.isPresent()) {
      byte[] output = _fileBytes.get();
      
      final String contentDisposition;
      if (!fileName.endsWith(".zip")) {
        output = LogFileService.zipBytes(fileName, output);
        contentDisposition = String.format("attachment; filename=%s.zip", fileName);
      } else {
        contentDisposition = String.format("attachment; filename=%s", fileName);
      }
      
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set("charset", "utf-8");
      responseHeaders.setContentType(MediaType.valueOf("text/html"));
      responseHeaders.setContentLength(output.length);
      responseHeaders.set("Content-disposition", contentDisposition);
      
      return new ResponseEntity<byte[]>(output, responseHeaders, HttpStatus.OK);
    } else {
      return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
    }
  }
  
  @GetMapping(path = "/delete/{file_name:.+}")
  public ResponseEntity<String> deleteLogFile(@PathVariable("file_name") String fileName, Model model) {
    log.debug("[deleteLogFile] fileName: {}", fileName);
    boolean deleteSuccess = service.deleteLogFile(fileName);
    return ResponseEntity.ok(deleteSuccess ? "ok" : "error");
  }
  
  @GetMapping(path = "/view/{file_name:.+}")
  public ResponseEntity<String> viewLogFile(@PathVariable("file_name") String fileName, Model model) {
    log.debug("[viewLogFile] fileName: {}", fileName);
    
    if (!service.checkCanViewOnline(fileName)) {
      return new ResponseEntity<String>("Cannot View On-line, please download zip files.", HttpStatus.BAD_REQUEST);
    }
    
    Optional<byte[]> _fileBytes = service.readLogFile(fileName);
    if (_fileBytes.isPresent()) {
      return ResponseEntity.ok(new String(_fileBytes.get()));
    } else {
      return new ResponseEntity<String>("File Not Found.", HttpStatus.NOT_FOUND);
    }
  }
  
}
