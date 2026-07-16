package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.dto.DTOConverter;
import com.governance.entity.Datasource;
import com.governance.entity.MetadataField;
import com.governance.entity.MetadataTable;
import com.governance.entity.TableChange;
import com.governance.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {
    private final MetadataService metadataService;

    @GetMapping("/datasources")
    public ApiResult<List<DTOConverter.DatasourceDTO>> listDatasources() {
        return ApiResult.ok(metadataService.listDatasources().stream()
            .map(DTOConverter.DatasourceDTO::from)
            .toList());
    }

    @PostMapping("/datasources")
    public ApiResult<DTOConverter.DatasourceDTO> createDatasource(@RequestBody Datasource datasource) {
        return ApiResult.ok(DTOConverter.DatasourceDTO.from(metadataService.saveDatasource(datasource)));
    }

    @PutMapping("/datasources/{id}")
    public ApiResult<DTOConverter.DatasourceDTO> updateDatasource(@PathVariable Long id, @RequestBody Datasource datasource) {
        datasource.setId(id);
        return ApiResult.ok(DTOConverter.DatasourceDTO.from(metadataService.saveDatasource(datasource)));
    }

    @DeleteMapping("/datasources/{id}")
    public ApiResult<Void> deleteDatasource(@PathVariable Long id) {
        metadataService.deleteDatasource(id);
        return ApiResult.ok();
    }

    @PostMapping("/datasources/{id}/collect")
    public ApiResult<Void> collectDatasource(@PathVariable Long id) {
        metadataService.collectDatasource(id);
        return ApiResult.ok();
    }

    @GetMapping("/tables")
    public ApiResult<List<DTOConverter.MetadataTableDTO>> listTables() {
        return ApiResult.ok(metadataService.listTables().stream()
            .map(DTOConverter.MetadataTableDTO::from)
            .toList());
    }

    @GetMapping("/tables/{id}")
    public ApiResult<DTOConverter.MetadataTableDTO> getTable(@PathVariable Long id) {
        return metadataService.getTable(id)
                .map(DTOConverter.MetadataTableDTO::from)
                .map(ApiResult::ok)
                .orElse(ApiResult.fail("Table not found"));
    }

    @GetMapping("/tables/{id}/fields")
    public ApiResult<List<DTOConverter.MetadataFieldDTO>> listFields(@PathVariable Long id) {
        return ApiResult.ok(metadataService.listFieldsByTable(id).stream()
            .map(DTOConverter.MetadataFieldDTO::from)
            .toList());
    }

    @GetMapping("/tables/{id}/changes")
    public ApiResult<List<DTOConverter.TableChangeDTO>> listChanges(@PathVariable Long id) {
        return ApiResult.ok(metadataService.listChangesByTable(id).stream()
            .map(DTOConverter.TableChangeDTO::from)
            .toList());
    }

    @GetMapping("/lineage")
    public ApiResult<Map<String, Object>> lineage() {
        return ApiResult.ok(metadataService.getLineage());
    }
}
