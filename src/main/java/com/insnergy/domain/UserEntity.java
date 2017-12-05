package com.insnergy.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "USER")
@Getter
@Setter
@EqualsAndHashCode(exclude = {})
@ToString(exclude = {})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  @Id
  @TableGenerator(name = "USER_INDEX_GEN", table = "HIBERNATE_SEQUENCES", pkColumnName = "SEQUENCE_NAME", valueColumnName = "NEXT_VAL", pkColumnValue = "USER_INDEX", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "USER_INDEX_GEN")
  private Long userIndex;
  
  @Column(name = "ID", unique = true)
  private String id;
  
  private String name;
  
  private String email;
  
  private String password;
  
  private String roles;
  
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "ownerUser", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ProjectEntity> projects;
  
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "ownerUser", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ApiEntity> apis;
  
}
