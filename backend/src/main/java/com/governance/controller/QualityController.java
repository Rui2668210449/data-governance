package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.dto.DTOConverter;
import com.governance.entity.QualityRule;
import com.governance.entity.QualityTicket;
import com.governance.service.QualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {
    private final QualityService qualityService;

    @GetMapping("/rules")
    public ApiResult<List<DTOConverter.QualityRuleDTO>> listRules() {
        return ApiResult.ok(qualityService.listRules().stream()
            .map(DTOConverter.QualityRuleDTO::from)
            .toList());
    }

    @PostMapping("/rules")
    public ApiResult<DTOConverter.QualityRuleDTO> createRule(@RequestBody QualityRule rule) {
        return ApiResult.ok(DTOConverter.QualityRuleDTO.from(qualityService.saveRule(rule)));
    }

    @PutMapping("/rules/{id}")
    public ApiResult<DTOConverter.QualityRuleDTO> updateRule(@PathVariable Long id, @RequestBody QualityRule rule) {
        rule.setId(id);
        return ApiResult.ok(DTOConverter.QualityRuleDTO.from(qualityService.saveRule(rule)));
    }

    @PostMapping("/rules/{id}/trial")
    public ApiResult<Object> trialRule(@PathVariable Long id) {
        return ApiResult.ok(qualityService.trialRule(id));
    }

    @GetMapping("/tickets")
    public ApiResult<List<DTOConverter.QualityTicketDTO>> listTickets() {
        return ApiResult.ok(qualityService.listTickets().stream()
            .map(DTOConverter.QualityTicketDTO::from)
            .toList());
    }

    @PutMapping("/tickets/{id}/status")
    public ApiResult<DTOConverter.QualityTicketDTO> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ApiResult.ok(DTOConverter.QualityTicketDTO.from(qualityService.updateTicketStatus(id, status)));
    }
}
