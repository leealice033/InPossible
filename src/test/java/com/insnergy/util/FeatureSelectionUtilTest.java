package com.insnergy.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import com.insnergy.service.rest.FeatureSelectionService.FeatureAttributeOutput;

public class FeatureSelectionUtilTest {
  
  @Ignore
  @Test
  public void doubleValueSetDegits() throws Exception {
    DecimalFormat df = new DecimalFormat("#.####");
    df.setRoundingMode(RoundingMode.CEILING);
    for (Number n : Arrays.asList(12, 123.12345, 0.23, 0.1, 2341234.212431324)) {
      Double d = n.doubleValue();
      System.out.println(df.format(d));
    }
    for (Number n : Arrays.asList(1.0, -0.16144503030503743, 0.8760599305148725, 0.8270161739282651)) {
      Double d = n.doubleValue();
      System.out.println(df.format(d));
    }
  }
  
  @Test
  public void test() throws Exception {
    List<FeatureAttributeOutput> featureAttributeOutputList = new ArrayList<>();
    
    List<Double> correlationListTest = new ArrayList<>();
    correlationListTest.add(1.0);
    correlationListTest.add(-0.16144503030503743);
    correlationListTest.add(0.8760599305148725);
    correlationListTest.add(0.8270161739282651);
    for (Double testData : correlationListTest) {
      System.out.println("correlationListTest1" + testData);
    }
    
    FeatureAttributeOutput test = FeatureAttributeOutput.builder()
                                                        .mean(3.704201680672269)
                                                        .standardDeviation(0.43984530693436874)
                                                        .averageCorrelation(0.6354077685345251)
                                                        .correlationList(correlationListTest)
                                                        .build();
    
    List<Double> correlationListTest2 = new ArrayList<>();
    correlationListTest2.add(1.0);
    correlationListTest2.add(-0.16144503030503743);
    correlationListTest2.add(0.8760599305148725);
    correlationListTest2.add(0.8270161739282651);
    for (Double testData : correlationListTest2) {
      System.out.println("correlationListTest2" + testData);
    }
    
    FeatureAttributeOutput test2 = FeatureAttributeOutput.builder()
                                                         .mean(3.065546218487396)
                                                         .standardDeviation(0.7970140830070748)
                                                         .averageCorrelation(0.5996475518603989)
                                                         .correlationList(correlationListTest2)
                                                         .build();
    
    featureAttributeOutputList.add(test);
    featureAttributeOutputList.add(test2);
    for (FeatureAttributeOutput testFeatureAttribute : featureAttributeOutputList) {
      System.out.println("testFeatureAttribute" + testFeatureAttribute);
    }
    
    DecimalFormat decimalFormat = new DecimalFormat("#.####");
    decimalFormat.setRoundingMode(RoundingMode.CEILING);
    for (FeatureAttributeOutput featureAttribute : featureAttributeOutputList) {
      String meanString = decimalFormat.format((featureAttribute.getMean()));
      Double m = Double.valueOf(meanString);
      String standardDeviationString = decimalFormat.format(featureAttribute.getStandardDeviation());
      String averageCorrelationString = decimalFormat.format(featureAttribute.getAverageCorrelation());
      List<String> correlationList = new ArrayList<>();
      System.out.println("meanStringFormat=" + meanString);
      System.out.println("meanDoubleFormat=" + m);
      System.out.println("standardDeviationString=" + standardDeviationString);
      System.out.println("averageCorrelationString=" + averageCorrelationString);
      
      for (Double correlation : featureAttribute.getCorrelationList()) {
        correlationList.add(decimalFormat.format(correlation));
      }
      for (String s : correlationList) {
        System.out.println("correlationList=" + s);
      }
    }
  }
  
}
