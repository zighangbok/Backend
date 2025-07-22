//package com.backend.zighangbok.domain.company.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Table(name = "company")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class Company {
//
//    @Id
//    private Integer id;  // PRIMARY KEY (AUTO_INCREMENT 아님)
//
//    @Column(length = 36, nullable = false)
//    private String uid;
//
//    @Column(name = "address_region", nullable = false)
//    private Integer addressRegion;
//
//    @Column(name = "company_address", length = 255, nullable = false)
//    private String companyAddress;
//
//    @Column(name = "company_name", length = 100, nullable = false)
//    private String companyName;  // 🔥 이게 JOIN에서 사용할 필드!
//
//    @Column(name = "company_type_id", nullable = false)
//    private Integer companyTypeId;
//
//    @Column(nullable = false)
//    private Integer hits;
//
//    @Column(name = "is_recruiting", nullable = false)
//    private Boolean isRecruiting;
//
//    // --- Nullable fields ---
//    @Column(name = "company_description", columnDefinition = "TEXT")
//    private String companyDescription;
//
//    @Column(name = "company_url", length = 255)
//    private String companyUrl;
//
//    @Column(name = "business_number", length = 20)
//    private String businessNumber;
//
//    @Column(name = "zip_code", length = 20)
//    private String zipCode;
//
//    @Column(name = "last_modified")
//    private LocalDateTime lastModified;
//}