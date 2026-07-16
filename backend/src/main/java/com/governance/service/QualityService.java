package com.governance.service;

import com.governance.entity.QualityRule;
import com.governance.entity.QualityTicket;
import com.governance.repository.QualityRuleRepository;
import com.governance.repository.QualityTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QualityService {
    private final QualityRuleRepository qualityRuleRepository;
    private final QualityTicketRepository qualityTicketRepository;

    public List<QualityRule> listRules() {
        return qualityRuleRepository.findAll();
    }

    public QualityRule saveRule(QualityRule rule) {
        return qualityRuleRepository.save(rule);
    }

    public Optional<QualityRule> getRule(Long id) {
        return qualityRuleRepository.findById(id);
    }

    public void deleteRule(Long id) {
        qualityRuleRepository.deleteById(id);
    }

    public Object trialRule(Long id) {
        return Map.of("ruleId", id, "result", "PASS", "sampleCount", 1000, "failCount", 0);
    }

    public List<QualityTicket> listTickets() {
        return qualityTicketRepository.findAll();
    }

    public Optional<QualityTicket> getTicket(Long id) {
        return qualityTicketRepository.findById(id);
    }

    public QualityTicket updateTicketStatus(Long id, String status) {
        QualityTicket ticket = qualityTicketRepository.findById(id).orElseThrow();
        ticket.setStatus(status);
        return qualityTicketRepository.save(ticket);
    }
}
