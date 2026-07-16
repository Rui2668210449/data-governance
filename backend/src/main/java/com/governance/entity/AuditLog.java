package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_user")
    private String auditUser;

    private String operation;

    private String target;

    @Column(name = "target_level")
    private String targetLevel;

    private String ip;

    private String status;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;
}
