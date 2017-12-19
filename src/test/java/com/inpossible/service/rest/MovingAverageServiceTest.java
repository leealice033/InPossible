package com.inpossible.service.rest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

public class MovingAverageServiceTest {
  private static String defaultCoin = "BTC/USD";
  private static String defaultZoom = "Day";
  @Autowired
  private MovingAverageService maService;
  
  @Ignore
  @Test
  public void test() {
    
    MovingAverage defaultSMA = MovingAverage.builder()
                                            .algorithm("SMA")
                                            .show(true)
                                            .period(30)
                                            .build();
    MovingAverage defaultEMA = MovingAverage.builder()
                                            .algorithm("EMA")
                                            .show(true)
                                            .period(30)
                                            .build();
    
    List<MovingAverage> defaultMaList = new ArrayList<>();
    defaultMaList.add(defaultSMA);
    defaultMaList.add(defaultEMA);
    
    PostDoMaInput defaultInput = PostDoMaInput.builder()
                                              .coin(defaultCoin)
                                              .zoom(defaultZoom)
                                              .ma(defaultMaList)
                                              .build();
    System.out.println("defaultInput=" + defaultInput);
    if (maService.postDoMovingAverage(defaultInput)
                 .isPresent()) {
      PostDoMaOutput doMaOutput = maService.postDoMovingAverage(defaultInput)
                                           .get();
      String imageUrl = doMaOutput.getImagePath();
      System.out.println("output=" + doMaOutput);
      System.out.println("ma_image=" + imageUrl);
      
    } else {
      System.out.println("fail");
    }
    
  }
  
}
