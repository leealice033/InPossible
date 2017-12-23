package com.inpossible.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.inpossible.service.rest.MachineLearningService;
import com.inpossible.service.rest.MachineLearningService.GetDoRegressionOutput;
import com.inpossible.service.rest.MachineLearningService.PredictResult;
import com.inpossible.service.rest.MovingAverageService;
import com.inpossible.service.rest.MovingAverageService.MovingAverage;
import com.inpossible.service.rest.MovingAverageService.PostDoMaInput;
import com.inpossible.service.rest.MovingAverageService.PostDoMaOutput;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class IndexController {
  // http://ntuesoe.com:8000"
  // ma/BTC/12hr/SMA_WMA/True_True/10_10
 private static final String ICAN = "http://ntuesoe.com:8000";
  //private static final String PYTHON = "http://127.0.0.1:8000";
  static final String PATH = "/inpossible";
  private final MovingAverageService maService;
  private final MachineLearningService mlService;
  private static String defaultCoin = "BTC";
  private static String defaultZoom = "24hr";// FIXME
  private static Integer defaultPeriod = 10;
  private static String defaultShows = "True_True";
  private static String defaultPeriods = "10_10";
  
  public IndexController(MovingAverageService maService, MachineLearningService mlService) {
    this.maService = maService;
    this.mlService = mlService;
  }
  
  // ma/BTC/12hr/SMA_WMA/True_True/10_10
  // index--default
  @GetMapping("/")
  public String toIndexPage(Model model, RedirectAttributes redirectAttributes) {
    log.debug("index");
    // log.debug("toIndexPage_coin={},zoom={},shows={},periods={}", coin, zoom,
    // shows, periods);
    
    MovingAverage defaultSMA = MovingAverage.builder()
                                            .algorithm("SMA")
                                            .show(true)
                                            .period(defaultPeriod)
                                            .build();
    
    MovingAverage defaultWMA = MovingAverage.builder()
                                            .algorithm("WMA")
                                            .show(true)
                                            .period(defaultPeriod)
                                            .build();
    
    List<MovingAverage> defaultMaList = new ArrayList<>();
    defaultMaList.add(defaultSMA);
    defaultMaList.add(defaultWMA);
    
    PostDoMaInput defaultInput = PostDoMaInput.builder()
                                              .coin(defaultCoin)
                                              .zoom(defaultZoom)
                                              .ma(defaultMaList)
                                              .build();
    // MA
    if (maService.postDoMovingAverage(defaultInput)
                 .isPresent()) {
      PostDoMaOutput doMaOutput = maService.postDoMovingAverage(defaultInput)
                                           .get();
      String imageUrl = buildImagePath(doMaOutput.getImagePath());
      log.debug("doMaOutput={}", doMaOutput);
      log.debug("AfterbuildImagePath={}", imageUrl);
      
      model.addAttribute("imageUrl", imageUrl);
    } else {
      log.error("fail_postDoMovingAverage");
    }
    
    // ML
    if (mlService.doRegressionPredict(defaultInput.getCoin())
                 .isPresent()) {
      GetDoRegressionOutput doRegressionOutput = mlService.doRegressionPredict(defaultInput.getCoin())
                                                          .get();
      log.debug("doRegressionOutput={}", doRegressionOutput);
      if (StringUtils.equals("ok", doRegressionOutput.getStatus())) {
        List<PredictResult> predictList = doRegressionOutput.getPredictOutput();
        log.debug("predictList={}", doRegressionOutput);
        model.addAttribute("predictList", predictList);
      } else {
        log.debug("Python status={}", doRegressionOutput.getStatus());
      }
      
    } else {
      log.error("fail_doRegressionPredict");
    }
    
    return "index";
    
  }
  
  // ma/BTC/12hr/SMA_WMA/True_True/10_10
  // TODO
  @GetMapping("/ma" + "/{coin:.+}" + "/{zoom:.+}" + "/SMA_WMA" + "/{shows:.+}" + "/{periods:.+}")
  public String refreshPage(@PathVariable String coin, @PathVariable String zoom, @PathVariable String shows,
      @PathVariable String periods, Model model, RedirectAttributes redirectAttributes) {
    log.debug("toIndexPage_coin={},zoom={},shows={},periods={}", coin, zoom, shows, periods);
    String[] showsArray = shows.split("_");
    String[] periodsArray = periods.split("_");
    List<Boolean> showsBoolean = new ArrayList<>();
    // List<String> periodsString = new ArrayList<>();
    for (int i = 0; i < showsArray.length; i++) {
      if (StringUtils.equals("true", showsArray[i])) {
        showsBoolean.add(true);
      } else if (StringUtils.equals("false", showsArray[i])) {
        showsBoolean.add(false);
      }
    }
    
    log.debug("showsBooleanList={}", showsBoolean);
    
    // BUILD MA INPUT
    MovingAverage sma_object = MovingAverage.builder()
                                            .algorithm("SMA")
                                            .show(showsBoolean.get(0))
                                            .period(Integer.valueOf(periodsArray[0]))
                                            .build();
    MovingAverage wma_object = MovingAverage.builder()
                                            .algorithm("WMA")
                                            .show(showsBoolean.get(1))
                                            .period(Integer.valueOf(periodsArray[1]))
                                            .build();
    log.debug("REFRESH-sma_object={}", sma_object);
    log.debug("REFRESH-wma_object={}", wma_object);
    
    List<MovingAverage> inputMaList = new ArrayList<>();
    inputMaList.add(sma_object);
    inputMaList.add(wma_object);
    PostDoMaInput maInput = PostDoMaInput.builder()
                                         .coin(coin)
                                         .zoom(zoom)
                                         .ma(inputMaList)
                                         .build();
    log.debug("REFRESH-maInput={}", maInput);
    // GET MA
    if (maService.postDoMovingAverage(maInput)
                 .isPresent()) {
      PostDoMaOutput doMaOutput = maService.postDoMovingAverage(maInput)
                                           .get();
      String imageUrl = buildImagePath(doMaOutput.getImagePath());
      log.debug("doMaOutput={}", doMaOutput);
      log.debug("AfterbuildImagePath={}", imageUrl);
      model.addAttribute("imageUrl", imageUrl);
    } else {
      log.error("fail_postDoMovingAverage");
    }
    
    //
    // GET ML
    if (mlService.doRegressionPredict(maInput.getCoin())
                 .isPresent()) {
      GetDoRegressionOutput doRegressionOutput = mlService.doRegressionPredict(maInput.getCoin())
                                                          .get();
      log.debug("doRegressionOutput={}", doRegressionOutput);
      if (StringUtils.equals("ok", doRegressionOutput.getStatus())) {
        List<PredictResult> predictList = doRegressionOutput.getPredictOutput();
        log.debug("predictList={}", doRegressionOutput);
        model.addAttribute("predictList", predictList);
      } else {
        log.debug("Python status={}", doRegressionOutput.getStatus());
      }
      
    } else {
      log.error("fail_doRegressionPredict");
    }
    
    return "index";
    
  }
  
  @GetMapping(path = PATH, params = "action=Refresh")
  public String getRefreshPage(String coin, String zoom, String ma_algorithm, Integer SMA_period, Integer WMA_period,
      Model model, RedirectAttributes redirectAttributes) {
    log.debug("REFRESH__fcoin={},zoom={},ma_algorithm={},SMA_period={},WMA_period={}", coin, zoom, ma_algorithm,
        SMA_period, WMA_period);
    MovingAverage sma_object = null;
    MovingAverage wma_object = null;
    if (ma_algorithm != null) {
      if (ma_algorithm.contains(",")) {
        String[] algorithmArray = ma_algorithm.split(",");
        log.debug("algorithmArray[0]={}", algorithmArray[0]);
        log.debug("algorithmArray[1]={}", algorithmArray[1]);
        sma_object = buildDefaultMAObject("SMA", true, SMA_period);
        wma_object = buildDefaultMAObject("WMA", true, WMA_period);
      } else {
        switch (ma_algorithm) {
          case "SMA":
            
            sma_object = buildDefaultMAObject("SMA", true, SMA_period);
            wma_object = buildDefaultMAObject("WMA", false, WMA_period);
            break;
          
          case "WMA":
            sma_object = buildDefaultMAObject("SMA", false, SMA_period);
            wma_object = buildDefaultMAObject("WMA", true, WMA_period);
            break;
          default:
            sma_object = buildDefaultMAObject("SMA", true, defaultPeriod);
            wma_object = buildDefaultMAObject("WMA", true, defaultPeriod);
            break;
        }
      }
      
    } else {
      sma_object = buildDefaultMAObject("SMA", true, defaultPeriod);
      wma_object = buildDefaultMAObject("WMA", true, defaultPeriod);
    }
    
    // MA
    List<MovingAverage> maList = new ArrayList<>();
    maList.add(sma_object);
    maList.add(wma_object);
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
    
    // ML
    if (mlService.doRegressionPredict(doMaInput.getCoin())
                 .isPresent()) {
      GetDoRegressionOutput doRegressionOutput = mlService.doRegressionPredict(doMaInput.getCoin())
                                                          .get();
      log.debug("doRegressionOutput={}", doRegressionOutput);
      if (StringUtils.equals("ok", doRegressionOutput.getStatus())) {
        List<PredictResult> predictList = doRegressionOutput.getPredictOutput();
        log.debug("predictList={}", doRegressionOutput);
        model.addAttribute("predictList", predictList);
      } else {
        log.debug("Python status={}", doRegressionOutput.getStatus());
      }
      
    } else {
      log.error("fail_doRegressionPredict");
    }
    
    return "index";
  }
  
  public String buildImagePath(String imageUrl) {
    String result = null;
    if (imageUrl != null) {
      result = ICAN + "/" + imageUrl;
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
