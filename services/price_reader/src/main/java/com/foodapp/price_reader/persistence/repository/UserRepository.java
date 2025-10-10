package com.foodapp.price_reader.persistence.repository;
import com.foodapp.price_reader.persistence.entity.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<UserEntity, String>{
   Optional<UserEntity> findByUsername(String username);
}
