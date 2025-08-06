package com.backend.zighangbok.domain.recruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RecruitmentSimpleDto {
    private String uuid;
    private String title;
    private String companyName;
}