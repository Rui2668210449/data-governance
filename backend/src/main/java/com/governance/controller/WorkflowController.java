package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.dto.DTOConverter;
import com.governance.entity.WorkflowTask;
import com.governance.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @GetMapping("/tasks")
    public ApiResult<List<DTOConverter.WorkflowTaskDTO>> listTasks() {
        return ApiResult.ok(workflowService.listTasks().stream()
            .map(DTOConverter.WorkflowTaskDTO::from)
            .toList());
    }

    @PutMapping("/tasks/{id}/status")
    public ApiResult<DTOConverter.WorkflowTaskDTO> updateTaskStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ApiResult.ok(DTOConverter.WorkflowTaskDTO.from(workflowService.updateTaskStatus(id, status)));
    }

    @GetMapping("/todo")
    public ApiResult<List<DTOConverter.WorkflowTaskDTO>> listTodoTasks() {
        return ApiResult.ok(workflowService.listTodoTasks().stream()
            .map(DTOConverter.WorkflowTaskDTO::from)
            .toList());
    }
}
