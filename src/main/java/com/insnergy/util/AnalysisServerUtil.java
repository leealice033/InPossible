package com.insnergy.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.insnergy.cofig.InAnalysisConfig;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class AnalysisServerUtil {
  
  public static String buildUrl(@NonNull InAnalysisConfig config, @NonNull AnalysisServer server, @NonNull String path,
      String... args) {
    log.debug("server={}, path={}, args={}", server, path, args);
    final StringBuilder urlBuilder = new StringBuilder();
    
    Optional<String> _serverUrl = getServerUrl(config, server);
    if (!_serverUrl.isPresent()) {
      log.error("no url for server[{}], config={}", server, config);
    }
    
    _serverUrl.ifPresent(serverUrl -> {
      urlBuilder.append(serverUrl);
      urlBuilder.append(path);
      
      if (args != null) {
        for (String arg : args) {
          if (StringUtils.isNotBlank(arg)) {
            urlBuilder.append("/" + arg);
          }
        }
      }
    });
    
    final String result = urlBuilder.toString();
    log.debug("buildUrl={}", result);
    return result;
  }
  
  private static final String ANALYSIS_SERVER_FILE_DOWNLOAD_PATH = "/download";
  
  public static String getCsvFileDownloadUrl(@NonNull InAnalysisConfig config, @NonNull AnalysisServer server,
      String fileId) {
    return AnalysisServerUtil.buildUrl(config, server, ANALYSIS_SERVER_FILE_DOWNLOAD_PATH, fileId);
  }
  
  public static Optional<String> getServerUrl(final InAnalysisConfig config, final AnalysisServer server) {
    final String url;
    switch (server) {
      case PYTHON:
        url = config.getPythonServerUrl();
        break;
      
      case R:
        url = config.getRServerUrl();
        break;
      
      default:
        url = null;
    }
    
    final Optional<String> result;
    if (UrlValidator.getInstance()
                    .isValid(url)) {
      result = Optional.ofNullable(url);
    } else {
      result = Optional.empty();
    }
    log.debug("getServerUrl[{}]: {}", server, result);
    return result;
  }
  
  public static boolean testServerAvailable(@NonNull final InAnalysisConfig config, final AnalysisServer server) {
    Optional<String> _url = getServerUrl(config, server);
    
    final boolean result;
    if (_url.isPresent()) {
      result = NetworkUtil.testUrl(_url.get());
    } else {
      result = false;
    }
    log.debug("testServerAvailable[{}]: {}", server, result);
    return result;
  }
  
}
