package com.backend.zighangbok.domain.recruitment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class RecruitmentListDto {

    private Integer id;        // 공고 ID
    private String title;      // 공고 제목
    private String companyName; // 회사명

    public RecruitmentListDto(Integer id, String title, String companyName) {
        this.id = id;
        this.title = title;
        this.companyName = companyName != null ? companyName : "회사명 없음";
    }
}