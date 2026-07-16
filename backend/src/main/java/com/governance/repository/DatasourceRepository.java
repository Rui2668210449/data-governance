package com.governance.repository;

import com.governance.entity.Datasource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasourceRepository extends JpaRepository<Datasource, Long> {
}
