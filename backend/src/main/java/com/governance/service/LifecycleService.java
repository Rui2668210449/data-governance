package com.governance.service;

import com.governance.entity.LifecyclePolicy;
import com.governance.repository.LifecyclePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LifecycleService {
    private final LifecyclePolicyRepository lifecyclePolicyRepository;

    public List<LifecyclePolicy> listPolicies() {
        return lifecyclePolicyRepository.findAll();
    }
}
