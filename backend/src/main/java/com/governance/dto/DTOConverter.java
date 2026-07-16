package com.governance.dto;

import com.governance.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class DTOConverter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(FORMATTER) : "";
    }

    public static List<String> parseTags(String tags) {
        return tags != null && !tags.isEmpty() ? Arrays.asList(tags.split(",")) : List.of();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataTableDTO {
        private Long id;
        private Long datasourceId;
        private String dbName;
        private String tableName;
        private String tableComment;
        private long rowCount;
        private long sizeBytes;
        private String owner;
        private List<String> tags;
        private double qualityScore;
        private String securityLevel;
        private String createTime;
        private String updateTime;

        public static MetadataTableDTO from(MetadataTable entity) {
            return MetadataTableDTO.builder()
                .id(entity.getId())
                .datasourceId(entity.getDatasourceId())
                .dbName(entity.getDbName())
                .tableName(entity.getTableName())
                .tableComment(entity.getTableComment())
                .rowCount(entity.getRowCount())
                .sizeBytes(entity.getSizeBytes())
                .owner(entity.getOwner())
                .tags(parseTags(entity.getTags()))
                .qualityScore(entity.getQualityScore())
                .securityLevel(entity.getSecurityLevel())
                .createTime(formatDateTime(entity.getCreateTime()))
                .updateTime(formatDateTime(entity.getUpdateTime()))
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasourceDTO {
        private Long id;
        private String name;
        private String type;
        private String jdbcUrl;
        private String username;
        private String status;
        private String lastCollectTime;
        private int tableCount;
        private String createTime;

        public static DatasourceDTO from(Datasource entity) {
            return DatasourceDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .jdbcUrl(entity.getJdbcUrl())
                .username(entity.getUsername())
                .status(entity.getStatus())
                .lastCollectTime(formatDateTime(entity.getLastCollectTime()))
                .tableCount(entity.getTableCount())
                .createTime(formatDateTime(entity.getCreateTime()))
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataFieldDTO {
        private Long id;
        private Long tableId;
        private String fieldName;
        private String dataType;
        private String comment;
        private String businessMeaning;
        private int isPrimary;
        private int isSensitive;

        public static MetadataFieldDTO from(MetadataField entity) {
            return MetadataFieldDTO.builder()
                .id(entity.getId())
                .tableId(entity.getTableId())
                .fieldName(entity.getFieldName())
                .dataType(entity.getDataType())
                .comment(entity.getComment())
                .businessMeaning(entity.getBusinessMeaning())
                .isPrimary(entity.getIsPrimary())
                .isSensitive(entity.getIsSensitive())
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableChangeDTO {
        private Long id;
        private Long tableId;
        private String changeType;
        private String changeDetail;
        private String version;
        private String changeTime;
        private String operator;

        public static TableChangeDTO from(TableChange entity) {
            return TableChangeDTO.builder()
                .id(entity.getId())
                .tableId(entity.getTableId())
                .changeType(entity.getChangeType())
                .changeDetail(entity.getChangeDetail())
                .version(entity.getVersion())
                .changeTime(formatDateTime(entity.getChangeTime()))
                .operator(entity.getOperator())
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityRuleDTO {
        private Long id;
        private String name;
        private String dimension;
        private String ruleType;
        private String ruleSql;
        private String targetTable;
        private int severity;
        private boolean enabled;
        private double lastScore;
        private String createTime;

        public static QualityRuleDTO from(QualityRule entity) {
            return QualityRuleDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .dimension(entity.getDimension())
                .ruleType(entity.getRuleType())
                .ruleSql(entity.getRuleSql())
                .targetTable(entity.getTargetTable())
                .severity(entity.getSeverity())
                .enabled(entity.isEnabled())
                .lastScore(entity.getLastScore())
                .createTime(formatDateTime(entity.getCreateTime()))
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityTicketDTO {
        private Long id;
        private Long resultId;
        private String title;
        private String status;
        private String assignee;
        private String priority;
        private String ruleName;
        private String description;
        private String createTime;
        private String updateTime;

        public static QualityTicketDTO from(QualityTicket entity) {
            return QualityTicketDTO.builder()
                .id(entity.getId())
                .resultId(entity.getResultId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .assignee(entity.getAssignee())
                .priority(entity.getPriority())
                .ruleName(entity.getRuleName())
                .description(entity.getDescription())
                .createTime(formatDateTime(entity.getCreateTime()))
                .updateTime(formatDateTime(entity.getUpdateTime()))
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTaskDTO {
        private Long id;
        private String title;
        private String type;
        private String status;
        private String assignee;
        private String priority;
        private String dueDate;
        private String description;
        private String createTime;

        public static WorkflowTaskDTO from(WorkflowTask entity) {
            return WorkflowTaskDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .status(entity.getStatus())
                .assignee(entity.getAssignee())
                .priority(entity.getPriority())
                .dueDate(formatDateTime(entity.getDueDate()))
                .description(entity.getDescription())
                .createTime(formatDateTime(entity.getCreateTime()))
                .build();
        }
    }
}
