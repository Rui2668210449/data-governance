package com.governance.repository;

import com.governance.entity.MaskPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaskPolicyRepository extends JpaRepository<MaskPolicy, Long> {
}
