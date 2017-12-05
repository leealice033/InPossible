package com.insnergy.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.insnergy.vo.CsvInfo.CsvInfoAction;

/**
 * [user_upload->data-preprocess] userId=iii, timestamp=1504753083,
 * stage=data-preprocess, function=source, value=user_upload
 */

// TODO
public class CsvActoinStringBuilderTest {
  
  // @Test
  public void testGetCsvColumnNames() throws Exception {
    List<String> fString = new ArrayList<>();
    CsvInfoAction fAction1 = CsvInfoAction.builder()
                                          .userId("iii")
                                          .timestamp(1504755571L)
                                          .stage("feature-selection")
                                          .function("source")
                                          .value("1fc9fd00-937e-11e7-8c28-408d5c5bc9ef")
                                          .build();
    CsvInfoAction fAction2 = CsvInfoAction.builder()
                                          .userId("iii")
                                          .timestamp(1504755571L)
                                          .stage("feature-selection")
                                          .function("feature_weight")
                                          .value("PearsonCorrelation")
                                          .build();
    CsvInfoAction fAction3 = CsvInfoAction.builder()
                                          .userId("iii")
                                          .timestamp(1504755571L)
                                          .stage("feature-selection")
                                          .function("feature_select")
                                          .value("total sulfur dioxide,density,pH,quality")
                                          .build();
    List<CsvInfoAction> featureSelectionAction = new ArrayList<>();
    featureSelectionAction.add(fAction1);
    featureSelectionAction.add(fAction2);
    featureSelectionAction.add(fAction3);
    
    // model
    /*
     * 
     * */
    for (CsvInfoAction temp : featureSelectionAction) {
      String s = actionToString(temp);
      System.out.println("string = " + '\n' + s);
    }
    
    // Date to timestamp
    
  }
  
  public String actionToString(CsvInfoAction action) {
    String result = null;
    String date = timestampTransform(action.getTimestamp());
    // UPLOAD FILE
    if (action.getFunction()
              .equals("source")) {
      if (action.getValue()
                .equals("user_upload")) {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n'
            + "You just uploaded file on your computer to stage: " + action.getStage() + "!";
      } else {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "You have change the file with ID:"
            + action.getValue();
      }
      
    }
    
    // FEA_SELECTION-feature_weight
    if (action.getFunction()
              .equals("feature_weight")) {
      result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "Feature weight by: "
          + action.getValue();
      
    }
    
    // FEA_SELECTION-feature_select
    if (action.getFunction()
              .equals("feature_select")) {
      result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "Selecting features are:"
          + action.getValue();
      
    }
    
    // DATA-PREPROCESS-
    if (action.getFunction()
              .equals("missing_value")) {
      if (action.getValue()
                .equals("TRUE")) {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n'
            + "You have filtered the missing value successfully!";
      } else {
        result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "Your file may have missing value.";
      }
      
    }
    
    // DATA-PREPROCESS-
    if (action.getFunction()
              .equals("normalize")) {
      result = "UserId: " + action.getUserId() + '\n' + "Time: " + date + '\n' + "Normalize by:" + action.getValue();
      
    }
    
    return result;
  }
  
  public String timestampTransform(Long timestamp) {
    String result = null;
    Date date = new Date(timestamp);
    Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    String dateString = format.format(date);
    System.out.println("format Date String =" + dateString);
    result = dateString;
    return result;
  }
  
  @Test
  public void testTime() {
    long t = 1504755572;
    System.out.println(t);
    System.out.println(new Date(t));
    long now = System.currentTimeMillis();
    System.out.println(now);
    System.out.println(new Date(now));
  }
  
  @Ignore
  @Test
  public void testActoinToString() throws Exception {
    
    CsvInfoAction fAction1 = CsvInfoAction.builder()
                                          .userId("iii")
                                          
                                          .stage("feature-selection")
                                          .function("source")
                                          .value("user_upload")
                                          .build();
    
    String expectOutput = "[2017-09-07 14:01:27] File Upload";
    String result = CsvActionStringBuilder.actionToString(fAction1);
    
    assertThat(result).isEqualTo(expectOutput);
    
  }
  
}
