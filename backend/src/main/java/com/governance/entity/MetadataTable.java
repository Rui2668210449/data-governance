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
@Table(name = "metadata_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id")
    private Long datasourceId;

    @Column(name = "db_name")
    private String dbName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "table_comment")
    private String tableComment;

    @Column(name = "row_count")
    private long rowCount;

    @Column(name = "size_bytes")
    private long sizeBytes;

    private String owner;

    private String tags;

    @Column(name = "quality_score")
    private double qualityScore;

    @Column(name = "security_level")
    private String securityLevel;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
