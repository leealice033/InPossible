package com.insnergy.service.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.insnergy.service.rest.AlgorithmService.AlgoParameterOutput;
import com.insnergy.service.rest.AlgorithmService.GetAlgoByProjectTypeOutput;
import com.insnergy.service.rest.AlgorithmService.GetAlgoParamOutput;
import com.insnergy.util.AnalysisServer;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class AlgorithmServiceTest {
  
  @Autowired
  private AlgorithmService algorithmService;
  
  @Test
  public void testGetAlgoByProjectType() throws Exception {
    List<String> mothodList = new ArrayList<String>();
    mothodList.add("knn");
    final GetAlgoByProjectTypeOutput correctOutput = GetAlgoByProjectTypeOutput.builder()
                                                                               .status("ok")
                                                                               .description(
                                                                                   "Get method list successfully")
                                                                               .projectType("classification")
                                                                               .algoList(mothodList)
                                                                               .build();
    AnalysisServer server = AnalysisServer.PYTHON;
    Optional<GetAlgoByProjectTypeOutput> getAlgoOutput = algorithmService.getAlgoByProjectType(server,
        "classification");
    System.out.println("output=" + getAlgoOutput);
    
    assertThat(getAlgoOutput).isPresent();
    assertThat(getAlgoOutput.get()).isEqualTo(correctOutput);
    
  }
  
  @Test
  public void testGetAlgoParameters() throws Exception {
    String projectType = "classification";
    String methodName = "knn";
    AlgoParameterOutput parameter1 = AlgoParameterOutput.builder()
                                                        .name("n_neighbors")
                                                        .type("int")
                                                        .range("")
                                                        .defaultValue("5")
                                                        .description("")
                                                        .build();
    
    AlgoParameterOutput parameter2 = AlgoParameterOutput.builder()
                                                        .name("weights")
                                                        .type("enum")
                                                        .range("uniform,distance")
                                                        .defaultValue("uniform")
                                                        .description("")
                                                        .build();
    
    List<AlgoParameterOutput> parameterList = new ArrayList<AlgoParameterOutput>();
    parameterList.add(parameter1);
    parameterList.add(parameter2);
    
    final GetAlgoParamOutput correctOutput = GetAlgoParamOutput.builder()
                                                               .status("ok")
                                                               .description("Get parameter successfully")
                                                               .projectType("classification")
                                                               .argumentsDef(parameterList)
                                                               .build();
    
    AnalysisServer server = AnalysisServer.PYTHON;
    Optional<GetAlgoParamOutput> getParamOutput = algorithmService.getAlgoParameters(server, methodName);
    
    assertThat(getParamOutput).isPresent();
    assertThat(getParamOutput.get()).isEqualTo(correctOutput);
    
  }
  
}
