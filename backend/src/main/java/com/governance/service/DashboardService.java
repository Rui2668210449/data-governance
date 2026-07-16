package com.governance.service;

import com.governance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DatasourceRepository datasourceRepository;
    private final MetadataTableRepository metadataTableRepository;
    private final MetadataFieldRepository metadataFieldRepository;
    private final QualityRuleRepository qualityRuleRepository;
    private final QualityTicketRepository qualityTicketRepository;
    private final WorkflowTaskRepository workflowTaskRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("datasourceCount", (int) datasourceRepository.count());
        stats.put("tableCount", (int) metadataTableRepository.count());
        stats.put("fieldCount", (int) metadataFieldRepository.count());

        double avgScore = qualityRuleRepository.findAll().stream().mapToDouble(q -> q.getLastScore() > 0 ? q.getLastScore() : 90).average().orElse(90);
        stats.put("qualityScore", Math.round(avgScore * 10) / 10.0);

        List<Map<String, Object>> trend = new ArrayList<>();
        String[] dates = {"07-01", "07-02", "07-03", "07-04", "07-05", "07-06", "07-07"};
        double[] scores = {88.2, 89.5, 90.1, 89.8, 91.3, 91.9, 92.6};
        for (int i = 0; i < dates.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", dates[i]);
            item.put("score", scores[i]);
            trend.add(item);
        }
        stats.put("qualityScoreTrend", trend);

        stats.put("todoCount", (int) workflowTaskRepository.findAll().stream().filter(t -> "TODO".equals(t.getStatus())).count());
        stats.put("issueCount", (int) qualityTicketRepository.findAll().stream().filter(t -> "OPEN".equals(t.getStatus()) || "PROCESSING".equals(t.getStatus())).count());

        List<Map<String, Object>> dist = new ArrayList<>();
        dist.add(Map.of("name", "MySQL", "value", 1456));
        dist.add(Map.of("name", "Hive", "value", 1280));
        dist.add(Map.of("name", "PostgreSQL", "value", 520));
        dist.add(Map.of("name", "REST API", "value", 230));
        stats.put("datasourceDistribution", dist);

        List<Map<String, Object>> growth = new ArrayList<>();
        growth.add(Map.of("date", "06月", "count", 3100));
        growth.add(Map.of("date", "07月", "count", 3486));
        stats.put("tableGrowth", growth);

        return stats;
    }
}
