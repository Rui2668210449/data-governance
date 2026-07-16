package com.governance.service;

import com.governance.entity.AuditLog;
import com.governance.entity.ClassifyLabel;
import com.governance.entity.MaskPolicy;
import com.governance.repository.AuditLogRepository;
import com.governance.repository.ClassifyLabelRepository;
import com.governance.repository.MaskPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final ClassifyLabelRepository classifyLabelRepository;
    private final MaskPolicyRepository maskPolicyRepository;
    private final AuditLogRepository auditLogRepository;

    public List<ClassifyLabel> listClassifyLabels() {
        return classifyLabelRepository.findAll();
    }

    public List<MaskPolicy> listMaskPolicies() {
        return maskPolicyRepository.findAll();
    }

    public MaskPolicy saveMaskPolicy(MaskPolicy policy) {
        return maskPolicyRepository.save(policy);
    }

    public List<AuditLog> listAuditLogs() {
        return auditLogRepository.findAll();
    }
}
