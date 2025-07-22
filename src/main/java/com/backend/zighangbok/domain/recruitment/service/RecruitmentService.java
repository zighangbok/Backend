package com.backend.zighangbok.domain.recruitment.service;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.dto.RecruitmentSimpleDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RecruitmentListDto> getRecruitmentList(int page, int size) {
        log.info("채용정보 조회 요청 - page: {}, size: {}", page, size);

        SearchRequest searchRequest = new SearchRequest("recruitment_parsed");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        int from = page * size;
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        sourceBuilder.from(from);
        sourceBuilder.size(size);

        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            List<RecruitmentListDto> result = new ArrayList<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();

                String uuid = (String) sourceAsMap.get("uuid");
                String title = (String) sourceAsMap.get("title");
                String companyJson = (String) sourceAsMap.get("company"); // company는 String 형태임

                String companyName = "회사명 없음";

                if (companyJson != null && !companyJson.isEmpty()) {
                    try {
                        // 작은따옴표를 큰따옴표로 변환
                        String fixedJson = companyJson.replace("'", "\"");
                        Map<String, Object> companyMap = objectMapper.readValue(fixedJson, new TypeReference<>() {});
                        if (companyMap.get("companyName") != null) {
                            companyName = companyMap.get("companyName").toString();
                        }
                    } catch (Exception e) {
                        log.warn("회사 정보 파싱 실패: {}", companyJson);
                    }
                }

                RecruitmentListDto dto = RecruitmentListDto.builder()
                        .uuid(uuid)
                        .title(title)
                        .company(companyJson)     // raw string도 넣어둠
                        .companyName(companyName) // 파싱된 회사명
                        .build();

                result.add(dto);

                log.debug("조회된 데이터 - uuid: {}, title: {}, companyName: {}", uuid, title, companyName);
            }

            log.info("총 조회된 문서 수: {}", response.getHits().getTotalHits().value);
            return result;

        } catch (IOException e) {
            log.error("OpenSearch 조회 실패", e);
            throw new RuntimeException("채용정보 조회 중 오류가 발생했습니다.", e);
        }
    }
    public List<RecruitmentSimpleDto> getSimpleRecruitmentList(int page, int size) {
        List<RecruitmentListDto> fullList = getRecruitmentList(page, size);

        return fullList.stream()
                .map(r -> new RecruitmentSimpleDto(r.getUuid(),r.getTitle(), r.getCompanyName()))
                .collect(Collectors.toList());
    }
}