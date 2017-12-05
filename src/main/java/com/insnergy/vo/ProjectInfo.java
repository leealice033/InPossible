package com.insnergy.vo;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  
  Long index;
  String deleteUrl;
  String id;
  String name;
  String type;
  List<CsvInfo> csvs;
  List<ModelInfo> models;
  
}
