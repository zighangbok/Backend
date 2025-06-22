package com.backend.zighangbok.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequestDto {
    @NotBlank(message = "아이디를 입력하세요")
    private String userId;
    @NotBlank(message = "비밀번호를 입력하세요")
    private String password;
}
