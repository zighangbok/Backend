package com.backend.zighangbok.domain.recruitment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Getter
@NoArgsConstructor
@ToString
public class Recruitment {

    private String id;

    private String uuid;

    private String title;

    private Map<String, Object> company;

    public String getCompanyName() {
        if (company != null) {
            Object companyNameObj = company.get("companyName");
            return companyNameObj != null ? companyNameObj.toString() : "회사명 없음";
        }
        return "회사명 없음";
    }
}