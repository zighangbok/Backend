package com.backend.zighangbok.domain.clicklog.controller;

import com.backend.zighangbok.domain.clicklog.dto.ClickLogRequestDto;
import com.backend.zighangbok.domain.clicklog.service.ClickLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/click-log")
public class ClickLogController {

    private final ClickLogService clickLogService;

    @PostMapping
    public ResponseEntity<Void> logClick(@RequestBody ClickLogRequestDto dto) {
        clickLogService.saveClickLog(dto);
        return ResponseEntity.ok().build();
    }
}