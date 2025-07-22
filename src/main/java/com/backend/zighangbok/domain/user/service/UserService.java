package com.backend.zighangbok.domain.user.service;

import com.backend.zighangbok.domain.user.dto.UserLoginRequestDto;
import com.backend.zighangbok.domain.user.dto.UserLoginResponseDto;
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
                .build();

        userRepository.save(user);
    }

    public UserLoginResponseDto login(UserLoginRequestDto request) {

        //유무 확인
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id 입니다"));

        //비밀번호 검증

        return UserLoginResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .build();

    }

    private void validateDuplicateUser(UserSignUpRequestDto request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
    }
}
