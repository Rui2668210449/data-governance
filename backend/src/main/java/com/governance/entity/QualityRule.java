package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "quality_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String dimension;

    @Column(name = "rule_type")
    private String ruleType;

    @Lob
    @Column(name = "rule_sql")
    private String ruleSql;

    @Column(name = "target_table")
    private String targetTable;

    private int severity;

    private boolean enabled;

    @Column(name = "last_score")
    private double lastScore;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
