package com.backend.zighangbok.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponseDto {
    private Long id;
    private String userId;
    private String name;
    private String nickname;
    private String email;
    private String message;
}
