package com.governance.repository;

import com.governance.entity.QualityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityRuleRepository extends JpaRepository<QualityRule, Long> {
}
