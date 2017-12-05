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
import java.util.Optional;

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
import com.insnergy.service.rest.ModelTrainingService;
import com.insnergy.service.rest.ModelTrainingService.ArgumentOfOneClassSVM;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewInput;
import com.insnergy.service.rest.ModelTrainingService.PostModelPreviewOutput;
import com.insnergy.service.rest.ModelTrainingService.PostModelTrainingInput;
import com.insnergy.service.rest.ModelTrainingService.PostModelTrainingOutput;
import com.insnergy.util.AnalysisServer;
import com.insnergy.vo.CsvInfo;
import com.insnergy.web.algo.ModelTrainingPageController;
import com.insnergy.web.algo.ModelTrainingPageController.PreviewParam;

//FIXME
@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ModelTrainingPageController.class)
public class ModelTrainingControllerTest {
  
  private static final String FAKE_FILE_ID = "FAKE_FILE_ID";
  private static final String FAKE_MODEL_NAME = "SVM_MODEL_NAME";
  private MockMvc mockMvc;
  
  @Autowired
  private WebApplicationContext webApplicationContext;
  
  @MockBean
  private CsvInfoService csvInfoService;
  
  @MockBean
  private ModelTrainingService modelTrainingService;
  
  final CsvInfo fakeCsvInfo = CsvInfo.builder()
                                     .server(AnalysisServer.PYTHON)
                                     .fileId(FAKE_FILE_ID)
                                     .fileName("FakeFileName")
                                     .columnNames(Arrays.asList("A", "B", "C"))
                                     .build();
  
  final ArgumentOfOneClassSVM fakeDefaulttOneClassSVM = ArgumentOfOneClassSVM.builder()
                                                                             .gamma(0.1)
                                                                             .nu(0.1)
                                                                             .kernel("rbf")
                                                                             .degree(2)
                                                                             .build();
  
  final PostModelTrainingInput fakeDefaultTrainingInput = PostModelTrainingInput.builder()
                                                                                .server(AnalysisServer.PYTHON)
                                                                                .fileId(FAKE_FILE_ID)
                                                                                .modelMethod("one-class SVM")
                                                                                .modelName(FAKE_MODEL_NAME)
                                                                                .argument(fakeDefaulttOneClassSVM)
                                                                                .build();
  
  final PostModelTrainingOutput fakePostModelTrainingOutput = PostModelTrainingOutput.builder()
                                                                                     .status("ok")
                                                                                     .description("TEST_OK")
                                                                                     .build();
  
  final PreviewParam fackPreviewParam = PreviewParam.builder()
                                                    .x(fakeCsvInfo.getColumnNames()
                                                                  .get(0))
                                                    .y(fakeCsvInfo.getColumnNames()
                                                                  .get(0))
                                                    .build();
  
  final PostModelPreviewInput fackPreviewInput = PostModelPreviewInput.builder()
                                                                      .server(fakeCsvInfo.getServer())
                                                                      .fileId(fakeCsvInfo.getFileId())
                                                                      .modelId(FAKE_MODEL_NAME)
                                                                      .xAxis(fackPreviewParam.getX())
                                                                      .yAxis(fackPreviewParam.getY())
                                                                      .build();
  
  final PostModelPreviewOutput fackPreviewOutput = PostModelPreviewOutput.builder()
                                                                         .status("ok")
                                                                         .description("TEST_OK")
                                                                         .imageUrl("www.google.com")
                                                                         .build();
  
  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build();
    
    // when(csvInfoService.findByFileId(FAKE_FILE_ID)).thenReturn(Optional.of(fakeCsvInfo));
    
    when(modelTrainingService.postModelTraining(fakeDefaultTrainingInput)).thenReturn(
        Optional.of(fakePostModelTrainingOutput));
    when(modelTrainingService.postModelPreview(fackPreviewInput)).thenReturn(Optional.of(fackPreviewOutput));
  }
  
  @Test
  public void testToModelTrainingPage() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/model-training"))
           .andExpect(status().isFound())
           .andExpect(view().name("redirect:/"))
           .andExpect(redirectedUrl("/"))
           .andExpect(flash().attribute("modelTrainingMessage", equalTo("error fileId")));
    
    mockMvc.perform(MockMvcRequestBuilders.get("/model-training/?fileId={fileId}", FAKE_FILE_ID))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("model-training"))
           .andExpect(model().attribute("sidebarActiveId", equalTo("sidebar-model-training")))
           .andExpect(model().attributeExists("csvInfo", "ONE_CLASS_SVM_KERNAL", "trainingInput"));
  }
  
  @Test
  public void testDoModelTraining() throws Exception {
    
    mockMvc.perform(MockMvcRequestBuilders.post("/model-training")
                                          .param("action", "Train")
                                          .param("fileId", FAKE_FILE_ID)
                                          .param("modelMethod", "One-class SVM")
                                          .param("argument.gamma", "" + fakeDefaultTrainingInput.getArgument()
                                                                                                .getGamma())
                                          .param("argument.nu", "" + fakeDefaultTrainingInput.getArgument()
                                                                                             .getNu())
                                          .param("argument.kernel", fakeDefaultTrainingInput.getArgument()
                                                                                            .getKernel())
                                          .param("argument.degree", "" + fakeDefaultTrainingInput.getArgument()
                                                                                                 .getDegree()))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("model-training"))
           .andExpect(model().attributeExists("csvInfo", "ONE_CLASS_SVM_KERNAL", "trainingInput", "previewParam"))
           .andDo(print());
  }
  
  @Test
  public void testDoModelpreview() throws Exception {
    
    testDoModelTraining();
    
    mockMvc.perform(MockMvcRequestBuilders.post("/model-training")
                                          .param("action", "Preview")
                                          .param("fileId", FAKE_FILE_ID)
                                          .param("x", fackPreviewInput.getXAxis())
                                          .param("y", fackPreviewInput.getYAxis()))
           .andExpect(status().isOk())
           .andExpect(content().contentType("text/html;charset=UTF-8"))
           .andExpect(view().name("model-training"))
           .andExpect(
               model().attributeExists("csvInfo", "previewParam", "imageUrl", "ONE_CLASS_SVM_KERNAL", "trainingInput"))
           .andDo(print());
    
  }
  
  @Test
  public void testCancel() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/model-training")
                                          .param("action", "Cancel"))
           .andExpect(status().is3xxRedirection());
  }
  
}
