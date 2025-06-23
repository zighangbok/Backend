package com.backend.zighangbok.domain.recruitment.controller;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
@Slf4j
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getRecruitmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        try {
            // 입력값 검증
            if (page < 0) page = 0;
            if (size < 1 || size > 100) size = 30;

            // 공고 리스트 조회
            List<RecruitmentListDto> recruitments = recruitmentService.getRecruitmentList(page, size);

            // 다음 페이지 존재 여부
            boolean hasMore = recruitmentService.hasMore(page, size);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", recruitments);           // 공고 리스트
            response.put("hasMore", hasMore);            // 다음 페이지 존재 여부
            response.put("currentPage", page);           // 현재 페이지
            response.put("count", recruitments.size());  // 현재 페이지 개수

            log.info("채용공고 리스트 조회 성공 - 페이지: {}, 개수: {}, 더보기: {}",
                    page, recruitments.size(), hasMore);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("채용공고 리스트 조회 실패 - 페이지: {}, 크기: {}", page, size, e);

            // 에러 응답
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "채용공고를 불러올 수 없습니다.");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", List.of());        // 빈 리스트
            errorResponse.put("hasMore", false);
            errorResponse.put("currentPage", page);
            errorResponse.put("count", 0);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}