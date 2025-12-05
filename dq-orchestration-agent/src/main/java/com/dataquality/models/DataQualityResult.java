package com.dataquality.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DataQualityResult {
    
    @JsonProperty("metric")
    private DataQualityMetric metric;
    
    @JsonProperty("score")
    private double score;
    
    @JsonProperty("threshold")
    private double threshold;
    
    @JsonProperty("passed")
    private boolean passed;
    
    @JsonProperty("issues")
    private List<String> issues;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    @JsonProperty("details")
    private Map<String, Object> details;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public DataQualityResult() {
        this.timestamp = LocalDateTime.now();
    }

    public DataQualityResult(DataQualityMetric metric, double score, double threshold) {
        this();
        this.metric = metric;
        this.score = score;
        this.threshold = threshold;
        this.passed = score >= threshold;
    }

    // Getters and Setters
    public DataQualityMetric getMetric() {
        return metric;
    }

    public void setMetric(DataQualityMetric metric) {
        this.metric = metric;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
        this.passed = score >= threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
        this.passed = score >= threshold;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}