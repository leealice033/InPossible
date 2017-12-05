package com.insnergy.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insnergy.domain.UserEntity;

public interface UserEntityRepo extends JpaRepository<UserEntity, Long> {
  
  Optional<UserEntity> findOneById(String id);
  
}
