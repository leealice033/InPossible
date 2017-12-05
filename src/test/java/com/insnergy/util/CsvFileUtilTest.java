package com.insnergy.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Ignore;
import org.junit.Test;

public class CsvFileUtilTest {
  
  @Ignore
  @Test
  public void testGetCsvColumnNames() throws Exception {
    String filePath = "DEMO.csv";
    // String filePath = "_warpop_v.csv";//紡織資料(漿紗)
    
    String[] columnNameList = CsvFileUtil.getCsvColumnNames(filePath);
    assertThat(columnNameList).isNotNull();
    System.out.println(Arrays.asList(columnNameList));
    // List<String> dataList = new ArrayList<>();
    for (int i = 0; i < columnNameList.length; i++) {
      List<String> dataList = new ArrayList<>();
      parseData("fileName", i, dataList);
      if (checkIntegerAndString(dataList)) {
        System.out.println("nonIntegerCaseHappen_index=" + i);
      }
      
    }
    // TODO 把合格的留下來
    
  }
  
  public static Boolean checkIntegerAndString(List<String> dataList) {
    Boolean result = false;
    for (String data : dataList) {
      if (NumberUtils.isParsable(data)) {
        if (data.contains(".")) {
          System.out.println("data is float=" + data);
          return result;
        } else if (data.equals("0")) {
          System.out.println("DataToCheck={},is zero:" + data);
          return result;
        }
      } else {
        System.out.println("DataToCheck={},is zero:" + data);
      }
      
    }
    result = true;
    return result;
  }
  
  public void parseByColumn() {
    List<String> dataList = new ArrayList<>();
    parseData("fileName", 2, dataList);
  }
  
  public static void parseData(String fileName, Integer columnIndex, List<String> dataList) {
    String filePath = "upload-dir/" + fileName;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line = null;
      int index = 0;
      while ((line = reader.readLine()) != null) {
        // case ','
        if (line.contains(",")) {
          String item[] = line.split(",");
          if (index > 0) {
            
            System.out.println("index:" + index + "->data =" + item[columnIndex]);
            dataList.add(item[columnIndex]);
            
          }
          
        }
        // case ';'
        else if (line.contains(";")) {
          String item[] = line.split(",");
          if (index > 0) {
            
            System.out.println("index:" + index + "->data =" + item[columnIndex]);
            dataList.add(item[columnIndex]);
            
          }
          
        }
        
        index++;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  @Ignore
  @Test
  public void test() throws Exception {
    String s1 = "123.0";
    System.out.println(NumberUtils.isParsable(s1));
    if (NumberUtils.isParsable(s1)) {
      System.out.println(NumberUtils.toDouble(s1));
      System.out.println(NumberUtils.toLong(s1));
    }
  }
  
  @Ignore
  @Test
  public void testWarpopCsvColumnNames() throws Exception {
    String filePath = "_warpop_v.csv";
    Integer indexYarnspec = -1;
    String[] result = CsvFileUtil.getCsvColumnNames(filePath);
    assertThat(result).isNotNull();
    System.out.println(Arrays.asList(result));
    for (int i = 0; i < result.length; i++) {
      if (result[i].equals("YARNSPEC")) {
        indexYarnspec = i;
        break;
      }
    }
    System.out.println("indexYarnSpec = " + indexYarnspec);
    
    // TODO find all YARNSPEC data
    getWarpopCsvYarnspecData(500, result.length, filePath, indexYarnspec);
    
  }
  
  public static void getWarpopCsvYarnspecData(int row, int col, String fileName, Integer indexYarnspec) {
    List<String> yarnspec = new ArrayList<>();
    String filePath = "upload-dir/" + fileName;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line = null;
      int index = 0;
      while ((line = reader.readLine()) != null && index < row) {
        // case ','
        if (line.contains(",")) {
          String item[] = line.split(",");
          if (index > 0) {
            
            System.out.println("data for yarnspec=" + item[indexYarnspec]);
            yarnspec.add(item[7]);
            
          }
          if (index == row - 1) {
            if (item.length >= col - 1) {
              String last = item[col - 1];
              System.out.println(last);
            }
          }
        }
        // case ';'
        else if (line.contains(";")) {
          String item[] = line.split(",");
          if (index > 0) {
            
            System.out.println("data for yarnspec=" + item[7]);
            yarnspec.add(item[7]);
            
          }
          if (index == row - 1) {
            if (item.length >= col - 1) {
              String last = item[col - 1];
              System.out.println(last);
            }
          }
        }
        
        index++;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
  public static ArrayList<Integer> checkLabel(Integer row, Integer col, String fileName, String projectType) {
    ArrayList<Integer> result = new ArrayList<>();
    String filePath = "upload-dir/" + fileName;
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      Integer checkZero = 0;
      String line = null;
      Integer index = 0;
      Integer indexForTypeCheck = 0;
      
      while ((line = reader.readLine()) != null && index < 2) {
        String item[] = splitByCommaOrSemico(line);
        for (String _temp : item) {
          
          if (_temp.equals("0"))
            
            checkZero = 1;
          break;
          
        }
        if (checkZero.equals(1)) {
          System.out.println("zeroCase_line={}" + line);
          line = reader.readLine();
          System.out.println("skipZeroCase_Newline={}" + line);
          
        }
        if (index > 0) {
          System.out.println("checksinceorow1_line={}" + line);
          
          Integer _indexForTypeCheck = 0;
          _indexForTypeCheck = indexForTypeCheck;
          caseCheck(projectType, _indexForTypeCheck, item, result);
        }
        if (index == row - 1) {
          if (item.length >= col - 1) {
            String last = item[col - 1];
            System.out.println(last);
          }
        }
        
        // log.debug("Index={},Test_Line={}", index, line);
        
        index++;
      }
      
    } catch (
    
    Exception e) {
      e.printStackTrace();
    }
    // log.debug("indexForProjectType={},indexForTypeStringListResult{}",
    // projectType, result);
    return result;
  }
  
  // case ';' return false
  public static boolean checkComma(String line) {
    if (line.contains(";")) {
      return false;
    }
    return true;
  }
  
  public static String[] splitByCommaOrSemico(String line) {
    // log.debug("Enter SplitByCommaOrNot method,line={}", line);
    String[] result = null;
    if (line.contains(";")) {
      result = line.split(";");
    } else if (line.contains(",")) {
      result = line.split(",");
    }
    for (String split : result)
      System.out.println("SplitByCommaOrNot method result = {}" + split);
    
    return result;
  }
  
  public static ArrayList<Integer> caseCheck(String projectType, int indexForTypeCheck, String[] line,
      ArrayList<Integer> result) {
    
    for (String data : line) {
      switch (projectType) {
        
        case "abnormal-detection":
          
          if (checkAbnormalLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
            // log.debug("indexFor[Abnormal]Checked={},data={}",
            // indexForTypeCheck, data);
          }
          break;
        
        case "regression":
          if (checkFloatLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
            // log.debug("indexFor[Regression]Checked={},data={}",
            // indexForTypeCheck, data);
          }
          break;
        
        case "classification":
          if (data.equals("0")) {
            System.out.println("0case_appears");
            break;
          }
          
          if (checkIntegerLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
          }
          
          break;
      }
      indexForTypeCheck++;
    }
    // log.debug("result_temp={}", result);//
    return result;
  }
  
  public static boolean checkStringAndNa(int index, String data) {
    if ((data.contains("\"")) || (data.equals("NA"))) {
      return true;
    } else
      return false;
  }
  
  public static boolean checkAbnormalLabel(int index, String data) {
    if ((data.equals("1")) || (data.equals("-1"))) {
      return true;
    } else
      return false;
  }
  
  public static boolean checkFloatLabel(int index, String data) {
    boolean result = false;
    if (NumberUtils.isParsable(data)) {
      result = true;
    }
    return result;
  }
  
  public static boolean checkIntegerLabel(int index, String data) {
    boolean result = false;
    if (NumberUtils.isParsable(data)) {
      if (data.contains(".")) {
        result = false;
      } else {
        result = true;
        
      }
    } else {
      // log.debug("DataToCheck={},is not Parsable", data);
    }
    // log.debug("DataToCheck={},isInt={}", index, result);
    return result;
  }
  
  public static Boolean hasValueZero(String[] item) {
    Boolean result = false;
    if (item != null) {
      for (String _zero : item) {
        if ((_zero != null) && _zero.equals("0"))
          result = true;
        return result;
      }
    }
    return result;
  }
  
  @Ignore
  @Test
  public void testForclasssification() throws Exception {
    List<String> validColumn = new ArrayList<>();
    System.out.println("test");
    String filePath = "forestfire.csv";
    String[] columnNameList = CsvFileUtil.getCsvColumnNames(filePath);
    System.out.println(Arrays.asList(columnNameList));
    for (int i = 0; i < columnNameList.length; i++) {
      List<String> dataList = new ArrayList<>();
      parseData(filePath, i, dataList);
      if (checkIntegerAndString(dataList)) {
        System.out.println("validIndex=" + i);
        System.out.println("validColumn=" + columnNameList[i]);
        validColumn.add(columnNameList[i]);
      }
      
    }
    
  }
  
  public String integerColumnName(List<String> list) {
    String result = null;
    return result;
  }
}
