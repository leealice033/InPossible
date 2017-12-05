package com.insnergy.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class KnnPredictionUtilTest {
  
  public static String[] testParseKnnClassificationReport(String s1) {
    // String s1 =
    // "label,precision,recall,f1-score,support;3,0.00,0.00,0.00,10;4,0.42,0.15,0.22,53;5,0.64,0.82,0.72,681;6,0.70,0.67,0.68,638;7,0.79,0.39,0.52,199;8,0.00,0.00,0.00,18;average,0.66,0.67,0.65,1599";
    String[] result = s1.split(";");
    System.out.println("testParseKnnClassificationReport_s1");
    for (String debug : result) {
      System.out.println("debugDebugDebugReport" + debug);
    }
    if (result.length > 0) {
      return result;
    }
    
    throw new RuntimeException("not yet implemented");
  }
  
  @Test
  public void testParse() throws Exception {
    String[] report = testParseKnnClassificationReport(
        "label,precision,recall,f1-score,support;3,0.00,0.00,0.00,10;4,0.42,0.15,0.22,53;5,0.64,0.82,0.72,681;6,0.70,0.67,0.68,638;7,0.79,0.39,0.52,199;8,0.00,0.00,0.00,18;average,0.66,0.67,0.65,1599");
    String[] dataTest = null;
    int countReport = 0;
    System.out.println("countReportreportInitial:" + countReport);
    System.out.println("report.length:" + report.length);
    int lineLenghth = report[0].length();
    dataTest = report[0].split(",");
    System.out.println("line_length:" + lineLenghth);//
    String[][] twoDimensionString = new String[report.length][dataTest.length];
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
    
    System.out.println("EndTwoDimensionalArray");
    
    System.out.println("mapMapMap");
    Map<Integer, List<String>> reportMap = doReportMapping(twoDimensionString);
    System.out.println("mapEnd");
    reportMap.forEach((k, v) -> System.out.println("Report_Key:" + k + "Report_Value" + v));
    
  }
  
  @Ignore
  public void parse(String[] report, int countReport, String[] dataTest) {
    String[][] twoDimensionString = new String[report.length][dataTest.length];
    for (int i = countReport; i < report.length; i++) {
      twoDimensionString[i][0] = dataTest[0];
      System.out.println(twoDimensionString[i][0] + ": ");
      for (int dataIndex = 1; dataIndex < dataTest.length; dataIndex++) {
        twoDimensionString[i][dataIndex] = dataTest[dataIndex];
        System.out.println(twoDimensionString[i][dataIndex] + " ");
      }
    }
    
  }
  
  @Ignore
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
