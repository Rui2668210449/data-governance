package com.governance.repository;

import com.governance.entity.DataStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataStandardRepository extends JpaRepository<DataStandard, Long> {
}
