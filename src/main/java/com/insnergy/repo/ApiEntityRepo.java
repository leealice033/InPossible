package com.insnergy.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insnergy.domain.ApiEntity;

public interface ApiEntityRepo extends JpaRepository<ApiEntity, Long> {
  
  Optional<ApiEntity> findOneById(String id);
  
  void deleteByApiIndex(Long apiIndex);
  
  void deleteById(String id);
}
