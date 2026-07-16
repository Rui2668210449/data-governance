package com.governance.repository;

import com.governance.entity.LifecyclePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LifecyclePolicyRepository extends JpaRepository<LifecyclePolicy, Long> {
    List<LifecyclePolicy> findByTableId(Long tableId);
}
