package com.insnergy.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.insnergy.cofig.InAnalysisConfig;
import com.insnergy.util.AnalysisServer;
import com.insnergy.util.AnalysisServerUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServerStatusCache {
  
  private final LoadingCache<AnalysisServer, Boolean> serverStatusCache;
  
  public ServerStatusCache(InAnalysisConfig config) {
    this.serverStatusCache = Caffeine.newBuilder()
                                     .maximumSize(2)
                                     .expireAfterWrite(5, TimeUnit.SECONDS)
                                     .build(key -> AnalysisServerUtil.testServerAvailable(config, key));
  }
  
  public Boolean getServerStatus(AnalysisServer server) {
    final Boolean result = serverStatusCache.get(server);
    log.debug("getServerStatus[{}]={}", server, result);
    return result;
  }
  
}
