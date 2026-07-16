package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_catalog")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id")
    private Long tableId;

    private String name;

    @Lob
    private String description;

    @Column(name = "catalog_path")
    private String catalogPath;

    private double rating;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "fav_count")
    private int favCount;

    private String owner;

    private String tags;

    private String type;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
