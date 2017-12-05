package com.insnergy.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class CsvFileDownloadApiTest {
  
  @Autowired
  private CsvFileDownloadApi api;
  
  @Ignore
  @Test
  public void test_download_csv_file() throws Exception {
    String fileName = "test.csv";
    
    log.info("testDownloadFile: {}", fileName);
    ResponseEntity<byte[]> resp = api.downloadFile(fileName);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getHeaders()
                   .get("Content-disposition")).isEqualTo(Arrays.asList("attachment; filename=test.csv.zip"));
    
    final String downloadFileUrl = api.getDownloadFileUrl("test.csv");
    log.info("download_url: {}", downloadFileUrl);
    assertThat(downloadFileUrl).endsWith("/file-zip/test.csv");
  }
  
  @Test
  public void test_file_not_found() throws Exception {
    String fileName = "NOT_FOUND_FILE.csv";
    
    log.info("testDownloadFile: {}", fileName);
    ResponseEntity<byte[]> resp = api.downloadFile(fileName);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
  
}
