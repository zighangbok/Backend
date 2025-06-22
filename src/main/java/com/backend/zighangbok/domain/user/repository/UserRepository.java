package com.backend.zighangbok.domain.user.repository;

import com.backend.zighangbok.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {  // ⭐ int → Long
    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
