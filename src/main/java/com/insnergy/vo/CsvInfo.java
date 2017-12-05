package com.insnergy.vo;

import java.io.Serializable;
import java.util.List;

import com.insnergy.util.AnalysisServer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CsvInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  
  Long index;
  
  AnalysisServer server;
  String downloadUrl;
  String deleteUrl;
  
  String userId;
  String stage;
  String fileId;
  String fileName;
  String projectId;
  String projectType;
  
  int row;
  int column;
  List<CsvInfoAction> actions;
  List<String> columnNames;
  String label;
  List<CsvInfoRow> rowValues;
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class CsvInfoAction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    String userId;
    Long timestamp;
    String stage;
    String function;
    String value;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class CsvInfoRow implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    int index;
    List<String> rowValue;
  }
  
}