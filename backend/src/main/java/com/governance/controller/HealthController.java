package com.governance.controller;

import com.governance.dto.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResult<Map<String, String>> health() {
        return ApiResult.ok(Map.of("status", "UP"));
    }
}
