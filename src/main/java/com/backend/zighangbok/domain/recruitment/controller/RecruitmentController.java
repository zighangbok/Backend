package com.backend.zighangbok.domain.recruitment.controller;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.dto.RecruitmentSimpleDto;
import com.backend.zighangbok.domain.recruitment.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @GetMapping
    public List<RecruitmentSimpleDto> getRecruitmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("API 호출 - 채용정보 목록 조회: page={}, size={}", page, size);
        return recruitmentService.getSimpleRecruitmentList(page, size);
    }
}