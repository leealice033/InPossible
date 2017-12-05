package com.insnergy.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.insnergy.vo.ModelInfo.ModelInfoAction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModelActionStringBuilder {
  
  public static String modelActionToString(ModelInfoAction action) {
    String result = null;
    
    String date = timestampTransform(action.getTimestamp());
    if (StringUtils.equals("source", action.getFunction())) {
      result = "UserId: " + action.getUserId() + "\n" + "Time: " + date + "\n" + "this model is trained by file(ID):"
          + action.getValue();
      
    } else if (StringUtils.equals("argument", action.getFunction())) {
      result = "Training argument: " + action.getValue();
    }
    return result;
    
  }
  
  public static String timestampTransform(Long timestamp) {
    String result = null;
    Date date = new Date(timestamp);
    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String dateString = format.format(date);
    log.debug("timestamp={},simpleDate={}", timestamp, dateString);
    result = dateString;
    return result;
  }
  
  public static String getModelCsvSource(ModelInfoAction action) {
    String result = null;
    if (StringUtils.equals("source", action.getFunction())) {
      result = action.getValue();
    }
    return result;
  }
}
