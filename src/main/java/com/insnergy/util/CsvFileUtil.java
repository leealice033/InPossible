package com.insnergy.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvFileUtil {
  
  public static String[] getCsvColumnNames(String fileName) {
    String filePath = "upload-dir/" + fileName;
    
    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));
      String firstLine = br.readLine();
      br.close();
      
      if (firstLine != null) {
        if (!firstLine.contains(";")) {
          return splitByCommaOrSemico(firstLine);
        } else {
          log.error("readLine error_has_Semico");
          String[] errorMessage = new String[] { "-1" };
          log.debug("error_string={}", errorMessage[0]);
          return errorMessage;
        }
      }
      
    } catch (Exception e) {
      log.error("readFirstLine error", e);
    }
    return null;
  }
  
  public static ArrayList<Integer> checkLabel(Integer row, Integer col, String fileName, String projectType) {
    
    ArrayList<Integer> result = new ArrayList<>();
    if(!(StringUtils.endsWith(fileName, ".csv"))){
      log.debug("checkLabel for no csv");
      return result;
    }
    String filePath = "upload-dir/" + fileName;
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(filePath));
      Integer checkZero = 0;
      String line = null;
      Integer index = 0;
      Integer indexForTypeCheck = 0;
      
      while ((line = reader.readLine()) != null && index < 2) {
        String item[] = splitByCommaOrSemico(line);
        for (String _temp : item) {
          log.debug("checkZeroFlag={}", checkZero);
          if (_temp.equals("0"))
            log.debug("ZEROcase={}", _temp);
          checkZero = 1;
          break;
          
        }
        if (checkZero.equals(1)) {
          log.debug("zeroCase_line={}", line);
          line = reader.readLine();
          log.debug("skipZeroCase_Newline={}", line);
          
        }
        if (index > 0) {
          log.debug("checksinceorow1_line={}", line);
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
        
        log.debug("Index={},Test_Line={}", index, line);
        
        index++;
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        // need to check for null
        if (reader != null) {
          reader.close();
          log.debug("close_buffer");
        }
        reader.close();
      } catch (IOException ex) {
        log.error("Problem occured. Cannot close reader : " + ex.getMessage());
      }
    }
    return result;
  }
  
  public static String[] splitByCommaOrSemico(String line) {
    log.debug("Enter SplitByCommaOrNot method,line={}", line);
    String[] result = null;
    if (line.contains(";")) {
      result = line.split(";");
    } else if (line.contains(",")) {
      result = line.split(",");
    }
    for (String split : result)
      log.debug("SplitByCommaOrNot method result = {}", split);
    return result;
  }
  
  public static ArrayList<Integer> caseCheck(String projectType, int indexForTypeCheck, String[] line,
      ArrayList<Integer> result) {
    log.debug("Enter caseCheck Method,param_projectType={},indexForTypeCheck={}, line={}", projectType,
        indexForTypeCheck, line);//
    for (String data : line) {
      switch (projectType) {
        
        case "abnormal-detection":
          
          if (checkAbnormalLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
            log.debug("indexFor[Abnormal]Checked={},data={}", indexForTypeCheck, data);
          }
          break;
        
        case "regression":
          if (checkFloatLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
            log.debug("indexFor[Regression]Checked={},data={}", indexForTypeCheck, data);
          }
          break;
        
        case "classification":
          if (data.equals("0")) {
            log.debug("0case_appears");
            break;
          }
          
          if (checkIntegerLabel(indexForTypeCheck, data)) {
            result.add(indexForTypeCheck);
            log.debug("indexFor[Classification]Checked={},data={}", indexForTypeCheck, data);
          }
          
          break;
      }
      indexForTypeCheck++;
    }
    log.debug("result_temp={}", result);//
    return result;
  }
  
  public static boolean checkStringAndNa(int index, String data) {
    if ((data.contains("\"")) || (data.equals("NA"))) {
      log.debug("checkedLabelString&NA={},data", index);
      return true;
    } else
      return false;
  }
  
  public static boolean checkAbnormalLabel(int index, String data) {
    if ((data.equals("1")) || (data.equals("-1"))) {
      log.debug("checkedLabel1/-1={},data", index);
      return true;
    } else
      return false;
  }
  
  public static boolean checkFloatLabel(int index, String data) {
    boolean result = false;
    log.debug("DataTtocheckFloat={}", data);
    if (NumberUtils.isParsable(data)) {
      result = true;
      log.debug("DataToCheck={},isFloat={}", data, NumberUtils.isParsable(data));
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
      log.debug("DataToCheck={},is not Parsable", data);
    }
    log.debug("DataToCheck={},isInt={}", index, result);
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
  
  public static List<String> classificationCase(String fileName) {
    List<String> validColumn = new ArrayList<>();
    System.out.println("test");
    String filePath = fileName;
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
    return validColumn;
  }
  
  public static void parseData(String fileName, Integer columnIndex, List<String> dataList) {
    String filePath = "upload-dir/" + fileName;
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(filePath));
      String line = null;
      int index = 0;
      while ((line = reader.readLine()) != null) {
        // case ','
        if (line.contains(",")) {
          String item[] = line.split(",");
          if (index > 0) {
            dataList.add(item[columnIndex]);
          }
        } else if (line.contains(";")) {
          String item[] = line.split(",");
          if (index > 0) {
            dataList.add(item[columnIndex]);
          }
        }
        index++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (reader != null) {
          reader.close();
          log.debug("close_buffer");
        }
        reader.close();
      } catch (IOException ex) {
        log.error("Problem occured. Cannot close reader : " + ex.getMessage());
      }
    }
    
  }
  
  public static Boolean checkIntegerAndString(List<String> dataList) {
    Boolean result = false;
    for (String data : dataList) {
      if (NumberUtils.isParsable(data)) {
        if (data.contains(".")) {
          System.out.println("data is float=" + data);
          return result;
          //FIXME
          
        } 
      } else {
        System.out.println("DataToCheck={}:" + data);
      }
      
    }
    result = true;
    return result;
  }
}
