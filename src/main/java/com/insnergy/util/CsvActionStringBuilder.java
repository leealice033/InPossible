package com.insnergy.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.insnergy.vo.CsvInfo.CsvInfoAction;

import lombok.extern.slf4j.Slf4j;

//TODO
@Slf4j
public class CsvActionStringBuilder {
  
  public static String actionToString(CsvInfoAction action) {
    
    String result = null;
    
    if (StringUtils.equals("source", action.getFunction()) && StringUtils.equals("user_upload", action.getValue())) {
      return String.format("[%s] File Upload", timestampTransform(action.getTimestamp()));
    }
    
    String date = timestampTransform(action.getTimestamp());
    
    if (StringUtils.equals("source", action.getFunction())) {
      if (action.getValue()
                .equals("user_upload")) {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n'
            + "You just uploaded file on your computer to stage: " + action.getStage() + "!";
      } else {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "You have change the file with ID:"
            + action.getValue();
      }
      
    }
    if (StringUtils.equals("feature_weight", action.getFunction())) {
      result = "Feature weight by: " + action.getValue();
      
    }
    
    if (StringUtils.equals("feature_select", action.getFunction())) {
      result = "Selecting features are:" + action.getValue();
      
    }
    
    if (StringUtils.equals("missing_value", action.getFunction())) {
      if (action.getValue()
                .equals("TRUE")) {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n'
            + "You have filtered the missing value successfully!";
      } else {
        result = "Your file may have missing value.";
      }
      
    }
    if (StringUtils.equals("normalize", action.getFunction())) {
      result = "Normalize by:" + action.getValue();
      
    }
    
    return result;
  }
  
  public static String timestampTransform(Long timestamp) {
    String result = null;
    Date date = new Date(timestamp);
    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String dateString = format.format(date);
    result = dateString;
    return result;
  }
}
