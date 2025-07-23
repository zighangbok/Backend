package com.backend.zighangbok.domain.clicklog.service;

import com.backend.zighangbok.domain.clicklog.dto.ClickLogRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickLogService {

    private final RestHighLevelClient client;

    public void saveClickLog(ClickLogRequestDto dto) {
        Map<String, Object> document = new HashMap<>();

        // 기존 인덱스 구조에 맞춰 flat하게 저장 (중첩 구조 X)
        document.put("properties.$device_id", dto.getDeviceId());
        document.put("properties.item_id", dto.getItemId());
        document.put("time", String.valueOf(System.currentTimeMillis())); // 기존에는 문자열로 저장되어 있음

        String id = "row_" + UUID.randomUUID();

        IndexRequest request = new IndexRequest("click_parsed")
                .id(id)
                .source(document);

        try {
            client.index(request, RequestOptions.DEFAULT);
            log.info("클릭 로그 저장 완료 - {}", id);
        } catch (IOException e) {
            log.error("클릭 로그 저장 실패", e);
            throw new RuntimeException("클릭 로그 저장 중 오류 발생", e);
        }
    }
}