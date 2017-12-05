package com.insnergy.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import com.insnergy.service.rest.FeatureSelectionService.FeatureAttributeOutput;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeatureSelectionUtil {
  
  public static void doubleValueSetDegits(List<FeatureAttributeOutput> featureAttributeOutputList) {
    DecimalFormat decimalFormat = new DecimalFormat("#.####");
    decimalFormat.setRoundingMode(RoundingMode.CEILING);
    for (FeatureAttributeOutput featureAttribute : featureAttributeOutputList) {
      String meanString = decimalFormat.format((featureAttribute.getMean()));
      featureAttribute.setMean(Double.valueOf(meanString));
      log.debug("meanDoubleFormat={}", featureAttribute.getMean());
      
      String standardDeviationString = decimalFormat.format(featureAttribute.getStandardDeviation());
      featureAttribute.setStandardDeviation(Double.valueOf(standardDeviationString));
      log.debug("StandardDeviationDoubleFormat={}", featureAttribute.getStandardDeviation());
      
      String averageCorrelationString = decimalFormat.format(featureAttribute.getAverageCorrelation());
      featureAttribute.setAverageCorrelation(Double.valueOf(averageCorrelationString));
      log.debug("AverageCorrelationDoubleFormat={}", featureAttribute.getAverageCorrelation());
      
      for (int i = 0; i < featureAttribute.getCorrelationList()
                                          .size(); i++) {
        featureAttribute.getCorrelationList()
                        .set(i, Double.valueOf(decimalFormat.format(featureAttribute.getCorrelationList()
                                                                                    .get(i))));
        
      }
      
      for (Double correlation : featureAttribute.getCorrelationList()) {
        log.debug("correlationListAfterFormat={}", correlation);
      }
    }
    
  }
}
