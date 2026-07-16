package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.entity.DataStandard;
import com.governance.service.StandardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/standard")
@RequiredArgsConstructor
public class StandardController {
    private final StandardService standardService;

    @GetMapping("/list")
    public ApiResult<List<DataStandard>> listStandards() {
        return ApiResult.ok(standardService.listStandards());
    }
}
