package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String type;

    private String status;

    private String assignee;

    private String priority;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Lob
    private String description;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
