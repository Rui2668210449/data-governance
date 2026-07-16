package com.governance.service;

import com.governance.entity.*;
import com.governance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final DatasourceRepository datasourceRepository;
    private final MetadataTableRepository metadataTableRepository;
    private final MetadataFieldRepository metadataFieldRepository;
    private final TableChangeRepository tableChangeRepository;

    public List<Datasource> listDatasources() {
        return datasourceRepository.findAll();
    }

    public Optional<Datasource> getDatasource(Long id) {
        return datasourceRepository.findById(id);
    }

    public Datasource saveDatasource(Datasource datasource) {
        return datasourceRepository.save(datasource);
    }

    public void deleteDatasource(Long id) {
        datasourceRepository.deleteById(id);
    }

    public void collectDatasource(Long id) {
        datasourceRepository.findById(id).ifPresent(ds -> {
            ds.setLastCollectTime(LocalDateTime.now());
            datasourceRepository.save(ds);
        });
    }

    public List<MetadataTable> listTables() {
        return metadataTableRepository.findAll();
    }

    public Optional<MetadataTable> getTable(Long id) {
        return metadataTableRepository.findById(id);
    }

    public List<MetadataTable> listTablesByDatasource(Long datasourceId) {
        return metadataTableRepository.findByDatasourceId(datasourceId);
    }

    public List<MetadataField> listFieldsByTable(Long tableId) {
        return metadataFieldRepository.findByTableId(tableId);
    }

    public List<TableChange> listChangesByTable(Long tableId) {
        return tableChangeRepository.findByTableId(tableId);
    }

    public Map<String, Object> getLineage() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(Map.of("id", "ods_user_log_d", "label", "ods.ods_user_log_d", "type", "TABLE", "dbName", "ods"));
        nodes.add(Map.of("id", "dwd_user_active_d", "label", "dwd.dwd_user_active_d", "type", "TABLE", "dbName", "dwd"));
        nodes.add(Map.of("id", "dws_user_active_m", "label", "dws.dws_user_active_m", "type", "TABLE", "dbName", "dws"));
        nodes.add(Map.of("id", "rpt_user_active_m", "label", "report.rpt_user_active_m", "type", "TABLE", "dbName", "report"));
        nodes.add(Map.of("id", "user_info", "label", "biz_core.user_info", "type", "TABLE", "dbName", "biz_core"));
        nodes.add(Map.of("id", "etl_task_1", "label", "ETL:用户活跃明细", "type", "TASK"));
        nodes.add(Map.of("id", "etl_task_2", "label", "ETL:用户活跃汇总", "type", "TASK"));
        nodes.add(Map.of("id", "etl_task_3", "label", "ETL:月活报表", "type", "TASK"));
        result.put("nodes", nodes);

        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(Map.of("source", "ods_user_log_d", "target", "etl_task_1"));
        edges.add(Map.of("source", "user_info", "target", "etl_task_1"));
        edges.add(Map.of("source", "etl_task_1", "target", "dwd_user_active_d"));
        edges.add(Map.of("source", "dwd_user_active_d", "target", "etl_task_2"));
        edges.add(Map.of("source", "etl_task_2", "target", "dws_user_active_m"));
        edges.add(Map.of("source", "dws_user_active_m", "target", "etl_task_3"));
        edges.add(Map.of("source", "etl_task_3", "target", "rpt_user_active_m"));
        result.put("edges", edges);

        return result;
    }
}
