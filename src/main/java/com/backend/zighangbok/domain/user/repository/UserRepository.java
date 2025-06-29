package com.backend.zighangbok.domain.user.repository;

import com.backend.zighangbok.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUserId(String userId);

    Optional<User> findByUserId(String userId);
}
