package com.insnergy.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import com.insnergy.util.FileUtil;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogFileService {
  
  private FileUtil fileUtil;
  
  final File logFileFolder = new File(System.getProperty("user.dir") + "/logs");
  
  public LogFileService(FileUtil fileUtil) {
    this.fileUtil = fileUtil;
  }
  
  @Data
  @Builder
  public static class LogFile {
    private String name;
    private String size;
    private String lastModified;
    private String path;
  }
  
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  public List<LogFile> getLogFiles() {
    log.debug("[getLogFiles]");
    final List<LogFile> logFileList = new ArrayList<>();
    
    fileUtil.getFileListInFolder(logFileFolder)
            .forEach(file -> {
              logFileList.add(LogFile.builder()
                                     .name(file.getName())
                                     .size(humanReadableByteCount(file.length(), true))
                                     .lastModified(sdf.format(file.lastModified()))
                                     .path(file.getPath())
                                     .build());
            });
    return logFileList;
  }
  
  public List<LogFile> getLogFiles(@NonNull final String filterName) {
    log.debug("[getLogFiles]");
    final List<LogFile> logFileList = new ArrayList<>();
    
    fileUtil.getFileListInFolder(logFileFolder)
            .stream()
            .filter(file -> StringUtils.containsIgnoreCase(file.getName(), filterName))
            .forEach(file -> {
              logFileList.add(LogFile.builder()
                                     .name(file.getName())
                                     .size(humanReadableByteCount(file.length(), true))
                                     .lastModified(sdf.format(file.lastModified()))
                                     .path(file.getPath())
                                     .build());
            });
    return logFileList;
  }
  
  String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit)
      return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
  
  /**
   * check file > 10MB can't view online
   */
  public boolean checkCanViewOnline(@NonNull String fileName) {
    final String filePath = logFileFolder.getAbsolutePath() + "/" + fileName;
    File logFile = new File(filePath);
    if (fileName.endsWith(".log") && logFile.exists() && logFile.length() < 10485760L)
      return true;
    else
      return false;
  }
  
  public Optional<byte[]> readLogFile(String fileName) {
    byte[] result = null;
    try {
      final String filePath = logFileFolder.getAbsolutePath() + "/" + fileName;
      result = Files.readAllBytes(Paths.get(filePath));
    } catch (IOException e) {
      log.error("readLogFile {} error: {}", fileName, ExceptionUtils.getMessage(e));
    }
    return Optional.ofNullable(result);
  }
  
  public static byte[] zipBytes(String fileName, byte[] input) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    ZipEntry entry = new ZipEntry(fileName);
    entry.setSize(input.length);
    try {
      zos.putNextEntry(entry);
      zos.write(input);
      zos.closeEntry();
      zos.close();
    } catch (IOException e) {
      log.error("to zip {} error: {}", fileName, ExceptionUtils.getMessage(e));
    }
    return baos.toByteArray();
  }
  
  public boolean deleteLogFile(String fileName) {
    final String filePath = logFileFolder.getAbsolutePath() + "/" + fileName;
    try {
      Files.deleteIfExists(Paths.get(filePath));
      return true;
    } catch (Exception e) {
      log.error("deleteLogFile {} error: {}", fileName, ExceptionUtils.getMessage(e));
    }
    return false;
  }
  
}
