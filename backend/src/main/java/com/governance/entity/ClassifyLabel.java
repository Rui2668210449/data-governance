package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classify_label")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifyLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String level;

    private String rules;

    @Column(name = "field_count")
    private int fieldCount;
}
