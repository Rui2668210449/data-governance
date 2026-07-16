package com.governance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetCatalogDTO {
    private Long id;
    private Long tableId;
    private String name;
    private String description;
    private String catalogPath;
    private double rating;
    private int viewCount;
    private int favCount;
    private String owner;
    private List<String> tags;
    private String type;
    private String updateTime;

    public static AssetCatalogDTO from(com.governance.entity.AssetCatalog entity) {
        List<String> tagList = entity.getTags() != null 
            ? Arrays.asList(entity.getTags().split(",")) 
            : List.of();
        
        String updateTime = entity.getUpdateTime() != null 
            ? entity.getUpdateTime().toString().replace("T", " ") 
            : "";

        return AssetCatalogDTO.builder()
            .id(entity.getId())
            .tableId(entity.getTableId())
            .name(entity.getName())
            .description(entity.getDescription())
            .catalogPath(entity.getCatalogPath())
            .rating(entity.getRating())
            .viewCount(entity.getViewCount())
            .favCount(entity.getFavCount())
            .owner(entity.getOwner())
            .tags(tagList)
            .type(entity.getType())
            .updateTime(updateTime)
            .build();
    }
}
