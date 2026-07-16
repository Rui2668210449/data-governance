package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mask_policy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "mask_type")
    private String maskType;

    private String expression;

    private String preview;
}
