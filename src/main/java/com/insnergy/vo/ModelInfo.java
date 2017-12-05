package com.insnergy.vo;

import java.io.Serializable;
import java.util.List;

import com.insnergy.util.AnalysisServer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  
  Long index;
  
  AnalysisServer server;
  String userId;
  String modelId;
  String modelName;
  String deleteUrl;
  String modelMethod;
  String projectId;
  String projectType;//
  List<ModelInfoAction> actions;
  OneClassSvmArgument argument;
  List<String> columnNames;
  String label;//
  List<String> outputFormat;
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class ModelInfoAction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    String userId;
    String function;
    String value;
    String stage;
    Long timestamp;
    
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class OneClassSvmArgument implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @NonNull
    Double gamma;
    
    @NonNull
    Double nu;
    
    @NonNull
    String kernel;
    
    @NonNull
    Integer degree;
  }
  
}
