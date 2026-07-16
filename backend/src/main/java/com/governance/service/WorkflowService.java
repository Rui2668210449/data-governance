package com.governance.service;

import com.governance.entity.WorkflowTask;
import com.governance.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowTaskRepository workflowTaskRepository;

    public List<WorkflowTask> listTasks() {
        return workflowTaskRepository.findAll();
    }

    public Optional<WorkflowTask> getTask(Long id) {
        return workflowTaskRepository.findById(id);
    }

    public WorkflowTask updateTaskStatus(Long id, String status) {
        WorkflowTask task = workflowTaskRepository.findById(id).orElseThrow();
        task.setStatus(status);
        return workflowTaskRepository.save(task);
    }

    public List<WorkflowTask> listTodoTasks() {
        return workflowTaskRepository.findByStatus("TODO");
    }
}
