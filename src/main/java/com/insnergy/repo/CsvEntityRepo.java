package com.insnergy.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insnergy.domain.CsvEntity;

public interface CsvEntityRepo extends JpaRepository<CsvEntity, Long> {
  
  Optional<CsvEntity> findOneById(String id);
  
  void deleteByCsvIndex(Long csvIndex);
  
  void deleteById(String id);
}
