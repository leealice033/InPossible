package com.insnergy.vo;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

//TODO name email
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private Long index;
  
  @NonNull
  private String id;
  
  private String name;
  
  private String email;
  
  @NonNull
  private String password;
  
  @NonNull
  @Singular
  private Set<String> roles;
  
  @NonNull
  private Map<String, ProjectInfo> projects;
  
  @NonNull
  private Map<String, ApiInfo> apis;
  
}
