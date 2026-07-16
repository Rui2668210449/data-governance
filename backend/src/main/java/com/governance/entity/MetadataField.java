package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metadata_field")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "data_type")
    private String dataType;

    private String comment;

    @Column(name = "business_meaning")
    private String businessMeaning;

    @Column(name = "is_primary")
    private int isPrimary;

    @Column(name = "is_sensitive")
    private int isSensitive;
}
