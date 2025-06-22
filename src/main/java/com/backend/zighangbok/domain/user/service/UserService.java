package com.backend.zighangbok.domain.user.service;

import com.backend.zighangbok.domain.user.dto.UserSignUpRequestDto;
import com.backend.zighangbok.domain.user.entity.User;
import com.backend.zighangbok.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public void signUp(UserSignUpRequestDto request) {

        validateDuplicateUser(request);

        User user = User.builder()
                .userId(request.getUserId())
                .password(request.getPassword())
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .build();

        userRepository.save(user);
    }

    private void validateDuplicateUser(UserSignUpRequestDto request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
    }
}
