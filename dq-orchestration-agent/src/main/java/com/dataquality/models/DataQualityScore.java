package com.dataquality.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class DataQualityScore {
    
    @JsonProperty("overallScore")
    private double overallScore;
    
    @JsonProperty("metricScores")
    private Map<DataQualityMetric, Double> metricScores;
    
    @JsonProperty("grade")
    private String grade;

    public DataQualityScore() {}

    public DataQualityScore(double overallScore, Map<DataQualityMetric, Double> metricScores) {
        this.overallScore = overallScore;
        this.metricScores = metricScores;
        this.grade = calculateGrade(overallScore);
    }

    private String calculateGrade(double score) {
        if (score >= 0.95) return "A+";
        if (score >= 0.90) return "A";
        if (score >= 0.85) return "B+";
        if (score >= 0.80) return "B";
        if (score >= 0.75) return "C+";
        if (score >= 0.70) return "C";
        if (score >= 0.65) return "D+";
        if (score >= 0.60) return "D";
        return "F";
    }

    // Getters and Setters
    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
        this.grade = calculateGrade(overallScore);
    }

    public Map<DataQualityMetric, Double> getMetricScores() {
        return metricScores;
    }

    public void setMetricScores(Map<DataQualityMetric, Double> metricScores) {
        this.metricScores = metricScores;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}