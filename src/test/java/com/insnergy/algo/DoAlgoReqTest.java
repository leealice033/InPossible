package com.insnergy.algo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import com.insnergy.algo.DoAlgoReq.DoAlgoReqBuilder;

@Ignore
public class DoAlgoReqTest {
  
  @Test
  public void testToJson() throws Exception {
    // given
    final String file_id = "fake_file_id";
    
    Map<String, String> argumentsMap = new LinkedHashMap<>();
    argumentsMap.put("gamma", "0.2");
    argumentsMap.put("nu", "0.8");
    argumentsMap.put("kernel", "rbf");
    argumentsMap.put("degree", "5");
    
    DoAlgoReqBuilder builder = DoAlgoReq.builder();
    builder = builder.file_id(file_id)
                     .model_name("fake_model_name");
    
    for (String key : argumentsMap.keySet()) {
      builder = builder.argument(key, argumentsMap.get(key));
    }
    
    DoAlgoReq req = builder.build();
    
    // when
    final String json = req.toJson();
    System.out.println(json);
    
    // than
    assertThat(json).isEqualTo(
        "{\"file_id\":\"fake_file_id\",\"model_name\":\"fake_model_name\",\"arguments\":{\"gamma\":\"0.2\",\"nu\":\"0.8\",\"kernel\":\"rbf\",\"degree\":\"5\"}}");
  }
  
}
