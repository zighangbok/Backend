package com.backend.zighangbok.domain.recruitment.service;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public List<RecruitmentListDto> getRecruitmentList(int page, int size) {
        // 최신 업로드순으로 정렬
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "uploadDate"));

        log.info("채용공고 조회 - 페이지: {}, 크기: {}", page, size);

        return recruitmentRepository.findRecruitmentList(pageable);
    }

    public long getTotalCount() {
        return recruitmentRepository.countActiveRecruitments();
    }

    public boolean hasMore(int page, int size) {
        long totalCount = getTotalCount();
        long currentCount = (long) (page + 1) * size;
        return currentCount < totalCount;
    }
}