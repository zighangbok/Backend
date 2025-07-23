package com.backend.zighangbok.domain.clicklog.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClickLogRequestDto {
    private String deviceId;  // $device_id
    private String itemId;    // uuid
}