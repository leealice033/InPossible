package com.insnergy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFormatUtil {
  public static void apiInputToJson(MakeApiInputFormat inputFormat, List<String> jsonList) {
    log.debug("apiInputToJson");
    String userDefineFeature = inputFormat.getUserDefineFeatureName();
    String type = inputFormat.getType();
    String temp = "\"" + userDefineFeature + '\"' + ":" + type + ",";
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
    
  }
  
  public static void lastInputToJson(MakeApiInputFormat inputFormat, List<String> jsonList) {
    log.debug("lastApiInputToJson");
    String userDefineFeature = inputFormat.getUserDefineFeatureName();
    String type = inputFormat.getType();
    String temp = "\"" + userDefineFeature + '\"' + ":" + type;
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
  }
  
  public static void apiOutputToJson(MakeApiOutputFormat outputFormat, List<String> jsonList) {
    log.debug("apiOutputToJson");
    String description = outputFormat.getDescription();
    String userDefineOutput = outputFormat.getUserDefineOutpuName();
    String temp = "\"" + userDefineOutput + '\"' + ":" + '\"' + description + '\"' + ",";
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
  }
  
  public static void lastOutputToJson(MakeApiOutputFormat outputFormat, List<String> jsonList) {
    log.debug("lastOutputToJson");
    String description = outputFormat.getDescription();
    String userDefineOutput = outputFormat.getUserDefineOutpuName();
    String temp = "\"" + userDefineOutput + '\"' + ":" + '\"' + description + '\"';
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
  }
  
  
  //TODO
  public List<String> buildTrainingReqJson() {
    List<String> result = new ArrayList<String>();
    return result;
  }
}
