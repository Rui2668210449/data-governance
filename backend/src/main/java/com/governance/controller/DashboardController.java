package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> stats() {
        return ApiResult.ok(dashboardService.getStats());
    }
}
