package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.entity.AuditLog;
import com.governance.entity.ClassifyLabel;
import com.governance.entity.MaskPolicy;
import com.governance.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {
    private final SecurityService securityService;

    @GetMapping("/classify")
    public ApiResult<List<ClassifyLabel>> listClassifyLabels() {
        return ApiResult.ok(securityService.listClassifyLabels());
    }

    @GetMapping("/mask/policies")
    public ApiResult<List<MaskPolicy>> listMaskPolicies() {
        return ApiResult.ok(securityService.listMaskPolicies());
    }

    @PostMapping("/mask/policies")
    public ApiResult<MaskPolicy> createMaskPolicy(@RequestBody MaskPolicy policy) {
        return ApiResult.ok(securityService.saveMaskPolicy(policy));
    }

    @GetMapping("/audit/logs")
    public ApiResult<List<AuditLog>> listAuditLogs() {
        return ApiResult.ok(securityService.listAuditLogs());
    }
}
