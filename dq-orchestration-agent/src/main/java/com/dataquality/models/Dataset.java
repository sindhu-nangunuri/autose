package com.dataquality.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Dataset {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("columns")
    private List<String> columns;
    
    @JsonProperty("data")
    private List<Map<String, Object>> data;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("rowCount")
    private int rowCount;
    
    @JsonProperty("columnCount")
    private int columnCount;

    public Dataset() {
        this.createdAt = LocalDateTime.now();
    }

    public Dataset(String id, String name, List<String> columns, List<Map<String, Object>> data) {
        this();
        this.id = id;
        this.name = name;
        this.columns = columns;
        this.data = data;
        this.rowCount = data != null ? data.size() : 0;
        this.columnCount = columns != null ? columns.size() : 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
        this.columnCount = columns != null ? columns.size() : 0;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
        this.rowCount = data != null ? data.size() : 0;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}