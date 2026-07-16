package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lifecycle_policy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifecyclePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_name")
    private String tableName;

    private String stage;

    @Column(name = "retain_days")
    private int retainDays;

    private String action;

    @Column(name = "size_bytes")
    private long sizeBytes;
}
