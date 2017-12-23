package com.inpossible.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;

import com.inpossible.service.rest.MovingAverageService;
import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = IndexController.class)
public class IndexControllerTest {
  private static String defaultCoin = "BTC";
  private static String defaultZoom = "24hr";
  @Autowired
  private MovingAverageService maService;
  
  @Test
  public void testGetRefreshPage(Model model) {
    /*
     * String coin, String zoom, String ma_algorithm, Integer SMA_period,
     * Integer WMA_period, Model model, RedirectAttributes redirectAttributes
     */
    // when
    MovingAverage defaultSMA = MovingAverage.builder()
                                            .algorithm("SMA")
                                            .show(true)
                                            .period(10)
                                            .build();
    MovingAverage defaultEMA = MovingAverage.builder()
                                            .algorithm("WMA")
                                            .show(true)
                                            .period(10)
                                            .build();
    
    List<MovingAverage> defaultMaList = new ArrayList<>();
    defaultMaList.add(defaultSMA);
    defaultMaList.add(defaultEMA);
    
    PostDoMaInput defaultInput = PostDoMaInput.builder()
                                              .coin(defaultCoin)
                                              .zoom(defaultZoom)
                                              .ma(defaultMaList)
                                              .build();
    
    // then
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
    assertThat(imageUrl).isNotNull();
    
  }
}
