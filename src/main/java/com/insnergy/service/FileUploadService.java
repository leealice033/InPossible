package com.insnergy.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.insnergy.cofig.InAnalysisConfig;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileUploadService {
  
  private final Path rootLocation;
  
  public FileUploadService(InAnalysisConfig config) {
    this.rootLocation = Paths.get(config.getCsvFileUploadDirectory());
  }
  
  @PostConstruct
  public void init() {
    try {
      Files.createDirectory(rootLocation);
      log.info("createDirectory={}", rootLocation);
    } catch (Exception e) {
    }
  }
  
  public void store(MultipartFile file) {
    try {
      if (file.isEmpty()) {
        throw new RuntimeException("Failed to store empty file " + file.getOriginalFilename());
      } else if (!file.getOriginalFilename()
                      .contains(".csv")) {
        log.debug("not csv file");
        throw new RuntimeException("Failed to store file " + file.getOriginalFilename());
      }
      Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
    }
  }
  
  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.rootLocation, 1)
                  .filter(path -> !path.equals(this.rootLocation))
                  .map(path -> this.rootLocation.relativize(path));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read stored files", e);
    }
  }
  
  public Path load(String filename) {
    return rootLocation.resolve(filename);
  }
  
  public Resource loadAsResource(String filename) {
    try {
      Path file = load(filename);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read file: " + filename);
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Could not read file: " + filename, e);
    }
  }
  
  public void deleteAll() {
    FileSystemUtils.deleteRecursively(rootLocation.toFile());
  }
  
}
