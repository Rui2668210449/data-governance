package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_standard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStandard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    @Column(name = "data_type")
    private String dataType;

    private int length;

    @Lob
    @Column(name = "business_meaning")
    private String businessMeaning;

    private String version;

    private String status;

    @Column(name = "mapping_count")
    private int mappingCount;

    @Column(name = "compliance_rate")
    private double complianceRate;
}
