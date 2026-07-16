package com.governance.service;

import com.governance.entity.AssetCatalog;
import com.governance.entity.AssetTag;
import com.governance.repository.AssetCatalogRepository;
import com.governance.repository.AssetTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final AssetCatalogRepository assetCatalogRepository;
    private final AssetTagRepository assetTagRepository;

    public List<AssetCatalog> searchAssets(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return assetCatalogRepository.findAll();
        }
        return assetCatalogRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    public Optional<AssetCatalog> getAsset(Long id) {
        return assetCatalogRepository.findById(id);
    }

    public List<AssetTag> listTags() {
        return assetTagRepository.findAll();
    }
}
