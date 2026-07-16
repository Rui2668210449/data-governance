package com.governance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "datasource")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Datasource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    @Column(name = "jdbc_url")
    private String jdbcUrl;

    private String username;

    private String password;

    private String status;

    @Column(name = "last_collect_time")
    private LocalDateTime lastCollectTime;

    @Column(name = "table_count")
    private int tableCount;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
