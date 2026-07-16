package com.governance.controller;

import com.governance.dto.ApiResult;
import com.governance.dto.AssetCatalogDTO;
import com.governance.entity.AssetTag;
import com.governance.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/search")
    public ApiResult<List<AssetCatalogDTO>> search(@RequestParam(required = false) String keyword) {
        return ApiResult.ok(catalogService.searchAssets(keyword).stream()
            .map(AssetCatalogDTO::from)
            .toList());
    }

    @GetMapping("/assets/{id}")
    public ApiResult<AssetCatalogDTO> getAsset(@PathVariable Long id) {
        return catalogService.getAsset(id)
                .map(AssetCatalogDTO::from)
                .map(ApiResult::ok)
                .orElse(ApiResult.fail("Asset not found"));
    }

    @GetMapping("/tags")
    public ApiResult<List<AssetTag>> listTags() {
        return ApiResult.ok(catalogService.listTags());
    }
}
