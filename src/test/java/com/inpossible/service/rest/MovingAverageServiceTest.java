package com.inpossible.service.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class MovingAverageServiceTest {
  private static String defaultCoin = "BTC/USD";
  private static String defaultZoom = "Day";
  @Autowired
  private MovingAverageService maService;
  
  @Test
  public void testGetMAChart() {
    
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
    String imageUrl = null;
    if (defaultInput != null) {
      System.out.println("defaultInput=" + defaultInput);
      if (maService.postDoMovingAverage(defaultInput)
                   .isPresent()) {
        PostDoMaOutput output = maService.postDoMovingAverage(defaultInput)
                                         .get();
        System.out.println("output=" + output);
        imageUrl = output.getImagePath();
      }
    } else {
      System.out.println("fail");
    }
    assertThat(imageUrl).isEqualTo("download/chart");
  }
  
}
