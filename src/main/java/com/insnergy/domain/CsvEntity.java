package com.insnergy.domain;

import java.io.Serializable;

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
@Table(name = "CSV")
@Getter
@Setter
@EqualsAndHashCode(exclude = { "ownerProject" })
@ToString(exclude = { "ownerProject" })
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvEntity implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  @Id
  @TableGenerator(name = "CSV_INDEX_GEN", table = "HIBERNATE_SEQUENCES", pkColumnName = "SEQUENCE_NAME", valueColumnName = "NEXT_VAL", pkColumnValue = "CSV_INDEX", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "CSV_INDEX_GEN")
  private Long csvIndex;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "projectIndex")
  private ProjectEntity ownerProject;
  
  @Column(name = "ID", unique = true)
  private String id;// fileId
  
  private String stage;
  
  private String label;
  
}
