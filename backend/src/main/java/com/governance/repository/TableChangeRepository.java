package com.governance.repository;

import com.governance.entity.TableChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableChangeRepository extends JpaRepository<TableChange, Long> {
    List<TableChange> findByTableId(Long tableId);
}
