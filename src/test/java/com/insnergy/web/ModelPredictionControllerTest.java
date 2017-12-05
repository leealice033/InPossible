package com.insnergy.web;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.insnergy.service.CsvInfoService;
import com.insnergy.service.rest.ModelPredictionService;
import com.insnergy.service.rest.ModelPredictionService.GetModelSearchInput;
import com.insnergy.service.rest.ModelPredictionService.GetModelSearchOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.CsvInfo;

//FIXME
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ModelPredictionController.class)
public class ModelPredictionControllerTest {
  
  private static final String FAKE_FILE_ID = "FAKE_FILE_ID";
  
  private MockMvc mockMvc;
  
  @Autowired
  private WebApplicationContext webApplicationContext;
  
  @MockBean
  private CsvInfoService csvFileInfoService;
  
  @MockBean
  private ModelPredictionService modelPredictionService;
  
  final CsvInfo fakeCsvFileInfo = CsvInfo.builder()
                                         .server(AnalysisServer.PYTHON)
                                         .fileId(FAKE_FILE_ID)
                                         .fileName("FakeFileName")
                                         .columnNames(Arrays.asList("A", "B", "C"))
                                         .build();
  
  final GetModelSearchInput fakeGetModelSearchInput = GetModelSearchInput.builder()
                                                                         .server(fakeCsvFileInfo.getServer())
                                                                         .build();
  
  final GetModelSearchOutput fakeGetModelSearchOutput = GetModelSearchOutput.builder()
                                                                            .status("test_ok")
                                                                            .description("test_desc")
                                                                            .model_list(Collections.emptyList())
                                                                            .build();
  
  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build();
    
    // when(csvFileInfoService.findByFileId(FAKE_FILE_ID)).thenReturn(Optional.of(fakeCsvFileInfo));
    
    when(modelPredictionService.getModelSearch(fakeGetModelSearchInput, FAKE_FILE_ID)).thenReturn(
        Optional.of(fakeGetModelSearchOutput));
  }
  
  @Ignore
  @Test
  public void testToModelPredictionPage() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/model-prediction"))
           .andExpect(status().isFound())
           .andExpect(view().name("redirect:/"))
           .andExpect(redirectedUrl("/"))
           .andExpect(flash().attribute("modePredictionMessage", equalTo("error fileId")));
    
    mockMvc.perform(MockMvcRequestBuilders.get("/model-prediction")
                                          .param("fileId", FAKE_FILE_ID))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("model-prediction"))
           .andExpect(model().attribute("sidebarActiveId", equalTo("sidebar-model-prediction")))
           .andExpect(model().attributeExists("modelList", "predictInputParam", "csvInfo"));
  }
  
  @Ignore
  @Test
  public void testDoModelPredict() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/model-prediction")
                                          .param("action", "Predict")
                                          .param("fileId", FAKE_FILE_ID)
                                          .param("modelId", "")
                                          .param("labelColumn", "B"))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("model-prediction"))
           .andExpect(content().string(Matchers.containsString("<div>Predict Error</div>")))
           .andDo(print());
  }
  
  @Ignore
  @Test
  public void testCancel() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/model-prediction")
                                          .param("action", "Cancel"))
           .andExpect(status().is3xxRedirection());
  }
  
  @Ignore
  @Test
  public void testDoClassificationModelPredict() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/model-prediction")
                                          .param("action", "Predict")
                                          .param("fileId", FAKE_FILE_ID)
                                          .param("modelId", ""))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("classification-model-prediction"))
           .andExpect(content().string(Matchers.containsString("<div>Predict Error</div>")))
           .andDo(print());
    throw new RuntimeException("not yet implemented");
  }
  
}
