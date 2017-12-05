package com.insnergy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KnnPredictionUtil {
  public static String[] parseKnnClassificationReport(String report) {
    log.debug("originalClassificationReport={}", report);
    String[] result = null;
    if (report != null) {
      result = report.split(";");
      for (String debug : result) {
        log.debug("originalClassificationReportSplitBy(';')={}", debug);
      }
      
    }
    return result;
  }
  
  // FIXME
  public static String[][] classificationReportOutput(String responseReport) {
    
    String[] report = parseKnnClassificationReport(responseReport);
    
    String[] dataTest = null;
    int lineLenghth = report[0].length();
    dataTest = report[0].split(",");
    System.out.println("line_length:" + lineLenghth);//
    String[][] twoDimensionString = new String[report.length][dataTest.length];
    
    int countReport = 0;
    System.out.println("countReportreportInitial:" + countReport);
    System.out.println("report.length:" + report.length);
    
    for (int i = countReport % (lineLenghth); i < report.length; i++) {
      System.out.println("i:" + countReport % (lineLenghth));
      twoDimensionString[i][0] = (report[i].split(","))[0];
      System.out.println(twoDimensionString[i][0] + ": ");
      for (int dataIndex = 1; dataIndex < dataTest.length; dataIndex++) {
        twoDimensionString[i][dataIndex] = (report[i].split(","))[dataIndex];
        System.out.println(twoDimensionString[i][dataIndex] + " ");
      }
      countReport++;
    }
    return twoDimensionString;
  }
  
  public static Map<Integer, List<String>> doReportMapping(String[][] classificationReport) {
    Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
    for (int i = 0; i < (classificationReport.length); i++) {
      Integer key = i;
      List<String> rowValue = new ArrayList<>();
      for (int j = 0; j < (classificationReport[0].length); j++) {
        if (classificationReport[i][j] != null) {
          rowValue.add(classificationReport[i][j]);
        }
      }
      if (rowValue != null) {
        System.out.println("rowValue:" + rowValue);
        map.put(key, rowValue);
      }
    }
    return map;
  }
}
