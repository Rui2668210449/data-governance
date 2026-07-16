package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_id")
    private Long resultId;

    private String title;

    private String status;

    private String assignee;

    private String priority;

    @Column(name = "rule_name")
    private String ruleName;

    @Lob
    private String description;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
