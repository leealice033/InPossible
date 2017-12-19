package com.inpossible.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inpossible.service.rest.MovingAverageService;
import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class IndexController {
  private static final String PYTHON = "http://127.0.0.1:8000";
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
      String imageUrl = buildImagePath(doMaOutput.getImagePath());
      log.debug("doMaOutput={}", doMaOutput);
      log.debug("AfterbuildImagePath={}", imageUrl);
      
      model.addAttribute("imageUrl", imageUrl);
    } else {
      log.error("fail");
    }
    return "index";
  }
  
  @PostMapping(path = PATH, params = "action=Refresh")
  public String updateImage(String coin, String zoom, String ma_algorithm, Integer SMA_period, Integer EMA_period,
      Model model, RedirectAttributes redirectAttributes) {
    log.debug("REFRESH__fcoin={},zoom={},ma_algorithm={},SMA_period={},EMA_period={}", coin, zoom, ma_algorithm, SMA_period,
        EMA_period);
    MovingAverage sma_object = null;
    MovingAverage ema_object = null;
    if (ma_algorithm != null) {
      if (ma_algorithm.contains(",")) {
        String[] algorithmArray = ma_algorithm.split(",");
        log.debug("algorithmArray[0]={}", algorithmArray[0]);
        log.debug("algorithmArray[1]={}", algorithmArray[1]);
        sma_object = buildDefaultMAObject("SMA", true, SMA_period);
        ema_object = buildDefaultMAObject("EMA", true, EMA_period);
      } else {
        switch (ma_algorithm) {
          case "SMA":
            
            sma_object = buildDefaultMAObject("SMA", true, SMA_period);
            ema_object = buildDefaultMAObject("EMA", false, EMA_period);
            break;
          
          case "EMA":
            sma_object = buildDefaultMAObject("SMA", false, SMA_period);
            ema_object = buildDefaultMAObject("EMA", true, EMA_period);
            break;
          default:
            sma_object = buildDefaultMAObject("SMA", true, 30);
            ema_object = buildDefaultMAObject("EMA", true, 30);
            break;
        }
      }
      
    } else {
      sma_object = buildDefaultMAObject("SMA", true, 30);
      ema_object = buildDefaultMAObject("EMA", true, 30);
    }
    
    List<MovingAverage> maList = new ArrayList<>();
    maList.add(sma_object);
    maList.add(ema_object);
    log.debug("maList_to_input={}", maList);
    PostDoMaInput doMaInput = PostDoMaInput.builder()
                                           .coin(coin)
                                           .zoom(zoom)
                                           .ma(maList)
                                           .build();
    
    if (maService.postDoMovingAverage(doMaInput)
                 .isPresent()) {
      PostDoMaOutput doMaOutput = maService.postDoMovingAverage(doMaInput)
                                           .get();
      String imageUrl = buildImagePath(doMaOutput.getImagePath());
      log.debug("refresh_doMaOutput={}", doMaOutput);
      log.debug("refresh_doMaOutput_AfterbuildImagePath={}", imageUrl);
      
      model.addAttribute("imageUrl", imageUrl);
    } else {
      log.error("fail");
    }
    
    return "index";
  }
  
  public String buildImagePath(String imageUrl) {
    String result = null;
    if (imageUrl != null) {
      result = PYTHON + "/" + imageUrl;
      log.debug("buildImagePath={}", result);
    }
    return result;
  }
  
  public MovingAverage buildDefaultMAObject(String name, Boolean show, Integer period) {
    return MovingAverage.builder()
                        .algorithm(name)
                        .show(show)
                        .period(period)
                        .build();
  }
  
}
