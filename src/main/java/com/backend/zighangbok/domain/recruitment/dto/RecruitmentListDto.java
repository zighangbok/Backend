package com.backend.zighangbok.domain.recruitment.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Collections;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RecruitmentListDto {

    private String id;
    private String uuid;
    private String title;

    // Elasticsearch 문서에서 받은 JSON 문자열
    private String company;

    private String companyName;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 작은따옴표를 큰따옴표로 바꿔서 JSON 파싱
    public Map<String, Object> getCompanyMap() {
        if (company == null || company.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            // 작은따옴표 → 큰따옴표, None → null 변환
            String json = company.replace("'", "\"").replace("None", "null");
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public String getCompanyName() {
        Map<String, Object> map = getCompanyMap();
        Object companyNameObj = map.get("companyName");
        return companyNameObj != null ? companyNameObj.toString() : "회사명 없음";
    }
}