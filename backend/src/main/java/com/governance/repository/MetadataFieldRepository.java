package com.governance.repository;

import com.governance.entity.MetadataField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetadataFieldRepository extends JpaRepository<MetadataField, Long> {
    List<MetadataField> findByTableId(Long tableId);
}
