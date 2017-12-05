package com.insnergy.api;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.insnergy.service.FileUploadService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(CsvFileDownloadApi.FILE_ZIP_PATH)
@Slf4j
public class CsvFileDownloadApi {
  
  public static final String FILE_ZIP_PATH = "/file-zip";
  
  private final FileUploadService fileUploadService;
  
  public CsvFileDownloadApi(FileUploadService fileUploadService) {
    this.fileUploadService = fileUploadService;
  }
  
  @GetMapping("/{fileName:.+}")
  public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
    log.info("downloadFile fileName: {}", fileName);
    
    Optional<byte[]> _fileBytes = readFile(fileName);
    if (_fileBytes.isPresent()) {
      byte[] output = _fileBytes.get();
      
      final String contentDisposition;
      
      if (!StringUtils.endsWith(fileName, ".zip")) {
        output = zipBytes(fileName, output);
        contentDisposition = String.format("attachment; filename=%s.zip", fileName);
      } else {
        contentDisposition = String.format("attachment; filename=%s", fileName);
      }
      
      final HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.set("charset", "utf-8");
      responseHeaders.setContentType(MediaType.valueOf("application/zip"));
      responseHeaders.setContentLength(output.length);
      responseHeaders.set("Content-disposition", contentDisposition);
      
      return new ResponseEntity<byte[]>(output, responseHeaders, HttpStatus.OK);
    } else {
      return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
    }
  }
  
  public String getDownloadFileUrl(final String fileName) {
    return MvcUriComponentsBuilder.fromMethodName(CsvFileDownloadApi.class, "downloadFile", fileName)
                                  .build()
                                  .toString();
  }
  
  private Optional<byte[]> readFile(String fileName) {
    byte[] result = null;
    try {
      result = Files.readAllBytes(fileUploadService.load(fileName));
    } catch (Exception e) {
      log.error("readFile {} error: {}", fileName, ExceptionUtils.getMessage(e));
    }
    return Optional.ofNullable(result);
  }
  
  private byte[] zipBytes(String fileName, byte[] input) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    ZipEntry entry = new ZipEntry(fileName);
    entry.setSize(input.length);
    try {
      zos.putNextEntry(entry);
      zos.write(input);
      zos.closeEntry();
      zos.close();
    } catch (Exception e) {
      log.error("to zip {} error: {}", fileName, ExceptionUtils.getMessage(e));
    }
    return baos.toByteArray();
  }
  
}
