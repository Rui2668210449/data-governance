package com.governance.repository;

import com.governance.entity.AssetCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetCatalogRepository extends JpaRepository<AssetCatalog, Long> {
    List<AssetCatalog> findByNameContainingOrDescriptionContaining(String name, String description);
}
