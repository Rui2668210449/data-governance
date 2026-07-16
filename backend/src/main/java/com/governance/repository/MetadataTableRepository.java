package com.governance.repository;

import com.governance.entity.MetadataTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataTableRepository extends JpaRepository<MetadataTable, Long> {
    List<MetadataTable> findByDatasourceId(Long datasourceId);
}
