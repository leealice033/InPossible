package com.insnergy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "API")
@Getter
@Setter
@EqualsAndHashCode(exclude = { "ownerUser" })
@ToString(exclude = { "ownerUser" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEntity {
  
  @Id
  @TableGenerator(name = "API_INDEX_GEN", table = "HIBERNATE_SEQUENCES", pkColumnName = "SEQUENCE_NAME", valueColumnName = "NEXT_VAL", pkColumnValue = "API_INDEX", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "API_INDEX_GEN")
  private Long apiIndex;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userIndex")
  private UserEntity ownerUser;
  
  @Column(name = "ID", unique = true)
  private String id;
  
  private String name;
  
  private String description;
  
  private String path;
  
  
}
