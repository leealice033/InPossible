package com.insnergy.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;

import com.insnergy.service.rest.MakeApiService.MakeApiInputFormat;
import com.insnergy.service.rest.MakeApiService.MakeApiOutputFormat;

public class JsonFormatUtilTest {
  
  /*
   * 
   * (outputName=15, description=15 Default Description, userDefineOutpuName=15
   * Defualt Name), (outputName=19,description=19 Default Description,
   * userDefineOutpuName=19 Defualt Name), (outputName=20, description=20
   * Default Description, userDefineOutpuName=20 Defualt Name)
   *
   */
  
  @Test
  public void Test() throws Exception {
    String json = null;
    MakeApiInputFormat inputFormat1 = MakeApiInputFormat.builder()
                                                        .featureName("FFMC")
                                                        .userDefineFeatureName("ffmc")
                                                        .type("float")
                                                        .description("description1")
                                                        .build();
    MakeApiInputFormat inputFormat2 = MakeApiInputFormat.builder()
                                                        .featureName("DMC")
                                                        .userDefineFeatureName("dmc")
                                                        .type("float")
                                                        .description("description2")
                                                        .build();
    
    MakeApiOutputFormat outputFormat1 = MakeApiOutputFormat.builder()
                                                           .outputName("A")
                                                           .userDefineOutpuName("classA")
                                                           .description("aa")
                                                           .build();
    MakeApiOutputFormat outputFormat2 = MakeApiOutputFormat.builder()
                                                           .outputName("B")
                                                           .userDefineOutpuName("classB")
                                                           .description("bb")
                                                           .build();
    MakeApiOutputFormat outputFormat3 = MakeApiOutputFormat.builder()
                                                           .outputName("C")
                                                           .userDefineOutpuName("classC")
                                                           .description("cc")
                                                           .build();
    List<MakeApiInputFormat> inputList = new ArrayList<>();
    List<MakeApiOutputFormat> outputList = new ArrayList<>();
    
    inputList.add(inputFormat1);
    inputList.add(inputFormat2);
    
    outputList.add(outputFormat1);
    outputList.add(outputFormat2);
    outputList.add(outputFormat3);
    List<String> inputJsonList = new ArrayList<>();
    List<String> outputJsonList = new ArrayList<>();
    for (MakeApiInputFormat input : inputList) {
      apiIntputToJson(input, inputJsonList);
    }
    
    for (MakeApiOutputFormat output : outputList) {
      apiOutputToJson(output, outputJsonList);
    }
  }
  
  // TODO
  /*
   * [(featureName=FFMC, userDefineFeatureName=a, type=float, description=FFMC),
   * (featureName=DMC, userDefineFeatureName=b, type=float, description=DMC),
   * (featureName=DC, userDefineFeatureName=c, type=float, description=DC),
   * (featureName=ISI, userDefineFeatureName=d, type=float, description=ISI)]
   */
  
  public static void apiIntputToJson(MakeApiInputFormat inputFormat, List<String> jsonList) {
    String result = null;
    String userDefineFeature = inputFormat.getUserDefineFeatureName();
    String type = inputFormat.getType();
    String temp = "\"" + userDefineFeature + '\"' + ":" + '\"' + type + '\"';
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
    
  }
  
  public static void apiOutputToJson(MakeApiOutputFormat outputFormat, List<String> jsonList) {
    String result = null;
    String description = outputFormat.getDescription();
    String userDefineOutput = outputFormat.getUserDefineOutpuName();
    String temp = "\"" + userDefineOutput + '\"' + ":" + '\"' + description + '\"';
    jsonList.add(temp);
    
    Stream<List<String>> listStream = Stream.of(jsonList);
    listStream.forEach(System.out::println);
  }
  
  @Ignore
  public void tiger() {
    String[] s = new String[] { "a", "b", "c" };
    Stream<String> ss = Stream.of(s);// Stream
                                     // https://docs.oracle.com/javase/8/docs/api/
    ss.map(data -> data.toUpperCase())
      .forEach(System.out::println);
    
  }
  
}