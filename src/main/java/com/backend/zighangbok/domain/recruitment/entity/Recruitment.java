package com.backend.zighangbok.domain.recruitment.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recruitment {

    @Id
    private Integer id;

    @Column(length = 36, nullable = false)
    private String uid;

    @Column(nullable = false)
    private Integer companyId;

    @Column(nullable = false)
    private LocalDateTime recruitmentStartDate;

    @Column(nullable = false)
    private Byte recruitmentDeadlineType;

    @Column(nullable = false)
    private LocalDateTime recruitmentDeadline;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String recruitmentAnnouncementLink;

    @Column(nullable = false)
    private Integer hits;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @Column(length = 50, nullable = false)
    private String textType;

    @Column(nullable = false)
    private Boolean isView;

    @Column(length = 255, nullable = false)
    private String shortenedUrl;

    // --- Nullable fields ---
    private Integer userId;

    @Column(length = 255)
    private String applicationEmail;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String preferentialTreatment;

    @Column(columnDefinition = "TEXT")
    private String qualification;

    @Column(length = 50)
    private String recUploadType;

    @Column(columnDefinition = "TEXT")
    private String recruitmentProcess;

    @Column(columnDefinition = "TEXT")
    private String teamInfo;

    @Column(columnDefinition = "TEXT")
    private String welfare;

    private LocalDate workStartDate;

    private LocalDate workEndDate;

    @Column(length = 255)
    private String affiliate;
}
