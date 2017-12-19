package com.inpossible.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.inpossible.service.rest.MovingAverageService;
import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class IndexController {
  static final String PATH = "/inpossible";
  private final MovingAverageService maService;
  private static String defaultCoin = "BTC/USD";
  private static String defaultZoom = "Day";
  
  public IndexController(MovingAverageService maService) {
    this.maService = maService;
  }
  
  @GetMapping("/")
  public String toIndexPage(Model model) {
    log.debug("enter_{}", PATH);
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
    if (maService.postDoMovingAverage(defaultInput)
                 .isPresent()) {
      PostDoMaOutput doMaOutput = maService.postDoMovingAverage(defaultInput)
                                           .get();
      String imageUrl = doMaOutput.getImage();
      log.debug("doMaOutput={}", doMaOutput);
      log.debug("doMaOutput_imageUrl={}", imageUrl);
      model.addAttribute("imageUrl",imageUrl);
    } else {
      log.error("fail");
    }
    return "index";
  }
  
}