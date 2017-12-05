package com.insnergy.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insnergy.domain.ProjectEntity;

public interface ProjectEntityRepo extends JpaRepository<ProjectEntity, Long> {
  
  Optional<ProjectEntity> findOneById(String id);
  
  void deleteByProjectIndex(Long projectIndex);
  
  void deleteById(String id);
}
