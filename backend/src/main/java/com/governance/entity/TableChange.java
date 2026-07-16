package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_change")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "change_detail")
    private String changeDetail;

    private String version;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    private String operator;
}
