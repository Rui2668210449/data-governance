package com.governance.repository;

import com.governance.entity.ClassifyLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassifyLabelRepository extends JpaRepository<ClassifyLabel, Long> {
}
