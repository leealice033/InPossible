package com.insnergy.util;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.routines.UrlValidator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class NetworkUtil {
  
  public static boolean testUrl(final String url) {
    boolean result = false;
    
    if (UrlValidator.getInstance()
                    .isValid(url)) {
      try {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if ((connection != null) && (connection.getResponseCode() != -1)) {
          result = true;
        }
        connection.disconnect();
      } catch (Exception e) {
        log.error("testUrl[{}] error: {}", url, ExceptionUtils.getMessage(e));
      }
    }
    log.debug("testUrl[{}]: {}", url, result);
    return result;
  }
  
}
