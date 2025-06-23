package com.backend.zighangbok.domain.recruitment.repository;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.entity.Recruitment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Integer> {

    @Query("""
        SELECT new com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto(
            r.id,
            r.title,
            c.companyName
        )
        FROM Recruitment r
        LEFT JOIN Company c ON r.companyId = c.id
        """)
    List<RecruitmentListDto> findRecruitmentList(Pageable pageable);

//   전체 공고 갯수 조회
    @Query("SELECT COUNT(r) FROM Recruitment r")
    long countActiveRecruitments();
}