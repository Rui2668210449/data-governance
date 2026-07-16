package com.governance.repository;

import com.governance.entity.AssetTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetTagRepository extends JpaRepository<AssetTag, Long> {
}
