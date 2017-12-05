package com.insnergy.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogRequestResponseFilter implements ClientHttpRequestInterceptor {
  
  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    
    logRequest(request, body);
    ClientHttpResponse clientHttpResponse = execution.execute(request, body);
    logResponse(clientHttpResponse);
    
    return clientHttpResponse;
  }
  
  private void logRequest(HttpRequest request, byte[] body) throws IOException {
    final String requestBody = getRequestBody(body);
    if (StringUtils.isBlank(requestBody)) {
      log.debug("REQ[{}]({})", request.getMethod(), request.getURI());
    } else {
      log.debug("REQ[{}]({}): {}", request.getMethod(), request.getURI(), requestBody);
    }
  }
  
  private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
    if (body != null && body.length > 0) {
      return (new String(body, "UTF-8"));
    } else {
      return null;
    }
  }
  
  private void logResponse(ClientHttpResponse response) throws IOException {
    log.debug("RES[{}-{}]: {}", response.getStatusCode(), response.getStatusText(), getBodyString(response));
  }
  
  private String getBodyString(ClientHttpResponse response) {
    try {
      if (response != null && response.getBody() != null) {
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        while (line != null) {
          inputStringBuilder.append(line);
          line = bufferedReader.readLine();
        }
        return inputStringBuilder.toString();
      } else {
        return null;
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
  
}