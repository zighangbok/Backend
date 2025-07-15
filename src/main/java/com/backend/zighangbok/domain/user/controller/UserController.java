package com.backend.zighangbok.domain.user.controller;

import com.backend.zighangbok.domain.user.dto.UserLoginRequestDto;
import com.backend.zighangbok.domain.user.dto.UserLoginResponseDto;
import com.backend.zighangbok.domain.user.dto.UserSignUpRequestDto;
import com.backend.zighangbok.domain.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody UserSignUpRequestDto request) {
        try {
            userService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();  // 201 Created만 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();  // 400 Bad Request만 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500만 반환
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto request, HttpSession session) {
        try {
            UserLoginResponseDto response = userService.login(request);

            session.setAttribute("userId", response.getUserId());

            return ResponseEntity.ok(response);
        }//사용자 에러
        catch (IllegalArgumentException e) {
            UserLoginResponseDto errorResponse = UserLoginResponseDto.builder()
                    .message(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }//시스템에러
        catch (Exception e) {
            UserLoginResponseDto errorResponse = UserLoginResponseDto.builder()
                    .message("로그인 오류")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }
}
