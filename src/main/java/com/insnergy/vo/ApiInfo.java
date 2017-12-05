package com.insnergy.vo;

import java.io.Serializable;
import java.util.List;

import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;
import com.insnergy.util.AnalysisServer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  
  Long index;
  
  AnalysisServer server;
  String userId;
  String projectId;
  String modelId;
  String apiId;
  String apiName;
  String apiDescription;
  String apiPath;
  String deleteUrl;
  int usageAmount;
  
  List<MakeApiInputFormat> inputFormats;
  List<MakeApiOutputFormat> outputFormats;
  StringBuilder inputJson;
  StringBuilder outputJson;
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ApiInfoInputFormat implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    String featureName;
    String userDefineFeatureName;
    String type;
    String description;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ApiInfoOutputFormat implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    String outputName;
    String userDefineOutputName;
    String description;
  }
}
