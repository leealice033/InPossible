package com.insnergy.algo;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class DoAlgoReq {
  String file_id;
  String model_name;
  
  @Singular
  Map<String, String> arguments;
  
  String toJson() {
    Gson gson = new GsonBuilder().serializeNulls()
                                 .create();
    return gson.toJson(this);
  }
}
