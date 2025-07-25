package com.backend.zighangbok.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RerankRequestDto {
    private String userId;
    private List<String> samples;
} 