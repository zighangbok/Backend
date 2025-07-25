package com.backend.zighangbok.domain.recruitment.controller;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.dto.RecruitmentSimpleDto;
import com.backend.zighangbok.domain.recruitment.service.RecruitmentService;
import com.backend.zighangbok.domain.recruitment.dto.RerankRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @GetMapping("/list")
    public List<RecruitmentListDto> getRecruitmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return recruitmentService.getRecruitmentList(page, size);
    }

    @GetMapping("/simple-list")
    public List<RecruitmentSimpleDto> getSimpleRecruitmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return recruitmentService.getSimpleRecruitmentList(page, size);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecruitmentSimpleDto>> getRecommendedRecruitments(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            // 로그인되지 않은 사용자는 접근할 수 없도록 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RecruitmentSimpleDto> recommendations = recruitmentService.getRecommendedRecruitments(userId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/recommendations/rerank")
    public ResponseEntity<Void> rerankRecommendations(@RequestBody RerankRequestDto rerankRequestDto) {
        String userId = rerankRequestDto.getUserId();

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return recruitmentService.rerankRecommendations(userId, rerankRequestDto.getSamples());
    }
}