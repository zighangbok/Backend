package com.backend.zighangbok.domain.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecruitmentSimpleDto {
    private String uuid;
    private String title;
    private String companyName;
}