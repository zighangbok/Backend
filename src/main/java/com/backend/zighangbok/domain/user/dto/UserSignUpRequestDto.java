package com.backend.zighangbok.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpRequestDto {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;
}
