package com.insnergy.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insnergy.domain.ModelEntity;

public interface ModelEntityRepo extends JpaRepository<ModelEntity, Long> {
  
  Optional<ModelEntity> findOneById(String id);
  
  void deleteByModelIndex(Long modelIndex);
  
  void deleteById(String id);
}
