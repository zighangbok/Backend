package com.backend.zighangbok.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpResponseDto {
    private int id;
    private String userId;
    private String name;
    private String nickname;
    private String email;
    private String message;
}
