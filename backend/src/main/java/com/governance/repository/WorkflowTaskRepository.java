package com.governance.repository;

import com.governance.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByStatus(String status);
}
