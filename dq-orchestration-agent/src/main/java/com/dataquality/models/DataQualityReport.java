package com.dataquality.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DataQualityReport {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("datasetName")
    private String datasetName;
    
    @JsonProperty("preProcessingScore")
    private DataQualityScore preProcessingScore;
    
    @JsonProperty("postProcessingScore")
    private DataQualityScore postProcessingScore;
    
    @JsonProperty("results")
    private List<DataQualityResult> results;
    
    @JsonProperty("rectificationActions")
    private List<String> rectificationActions;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("processingTimeMs")
    private long processingTimeMs;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public DataQualityReport() {
        this.timestamp = LocalDateTime.now();
    }

    public DataQualityReport(String id, String datasetName) {
        this();
        this.id = id;
        this.datasetName = datasetName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public DataQualityScore getPreProcessingScore() {
        return preProcessingScore;
    }

    public void setPreProcessingScore(DataQualityScore preProcessingScore) {
        this.preProcessingScore = preProcessingScore;
    }

    public DataQualityScore getPostProcessingScore() {
        return postProcessingScore;
    }

    public void setPostProcessingScore(DataQualityScore postProcessingScore) {
        this.postProcessingScore = postProcessingScore;
    }

    public List<DataQualityResult> getResults() {
        return results;
    }

    public void setResults(List<DataQualityResult> results) {
        this.results = results;
    }

    public List<String> getRectificationActions() {
        return rectificationActions;
    }

    public void setRectificationActions(List<String> rectificationActions) {
        this.rectificationActions = rectificationActions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}