package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.entity.LifecyclePolicy;
import com.governance.service.LifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lifecycle")
@RequiredArgsConstructor
public class LifecycleController {
    private final LifecycleService lifecycleService;

    @GetMapping("/policies")
    public ApiResult<List<LifecyclePolicy>> listPolicies() {
        return ApiResult.ok(lifecycleService.listPolicies());
    }
}
