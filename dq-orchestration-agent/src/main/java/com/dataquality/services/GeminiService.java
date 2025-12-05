package com.dataquality.services;

import com.dataquality.models.DataQualityReport;
import com.dataquality.models.DataQualityResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GeminiService {
    
    private static final Logger logger = Logger.getLogger(GeminiService.class);
    
    @ConfigProperty(name = "data-quality.gemini.api-key")
    String apiKey;
    
    @ConfigProperty(name = "data-quality.gemini.model", defaultValue = "gemini-pro")
    String model;
    
    @ConfigProperty(name = "data-quality.gemini.temperature", defaultValue = "0.1")
    double temperature;
    
    @ConfigProperty(name = "data-quality.gemini.max-tokens", defaultValue = "1000")
    int maxTokens;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String generateDataQualitySummary(DataQualityReport report) {
        logger.info("Generating data quality summary using Gemini for report: " + report.getId());
        
        try {
            String prompt = buildSummaryPrompt(report);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            logger.error("Failed to generate summary using Gemini", e);
            return generateFallbackSummary(report);
        }
    }
    
    public List<String> generateRecommendations(List<DataQualityResult> results) {
        logger.info("Generating recommendations using Gemini for " + results.size() + " results");
        
        try {
            String prompt = buildRecommendationsPrompt(results);
            String response = callGeminiAPI(prompt);
            return parseRecommendations(response);
        } catch (Exception e) {
            logger.error("Failed to generate recommendations using Gemini", e);
            return generateFallbackRecommendations(results);
        }
    }
    
    public String analyzeDataQualityTrends(DataQualityReport report) {
        logger.info("Analyzing data quality trends using Gemini");
        
        try {
            String prompt = buildTrendsPrompt(report);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            logger.error("Failed to analyze trends using Gemini", e);
            return "Unable to analyze trends at this time.";
        }
    }
    
    public String processUserPrompt(String userPrompt) {
        logger.info("Processing user prompt using Gemini: " + userPrompt.substring(0, Math.min(userPrompt.length(), 100)) + "...");
        
        try {
            String enhancedPrompt = buildUserPrompt(userPrompt);
            return callGeminiAPI(enhancedPrompt);
        } catch (Exception e) {
            logger.error("Failed to process user prompt using Gemini", e);
            return "I apologize, but I'm unable to process your request at this time. Please try again later or contact support if the issue persists.";
        }
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            ),
            "generationConfig", Map.of(
                "temperature", temperature,
                "maxOutputTokens", maxTokens
            )
        );
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .timeout(Duration.ofSeconds(60))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API call failed with status: " + response.statusCode());
        }
        
        return parseGeminiResponse(response.body());
    }
    
    private String parseGeminiResponse(String responseBody) throws Exception {
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> candidate = candidates.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            
            if (content != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        }
        
        throw new RuntimeException("Unable to parse Gemini response");
    }
    
    private String buildSummaryPrompt(DataQualityReport report) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a comprehensive data quality summary for the following report:\n\n");
        prompt.append("Dataset: ").append(report.getDatasetName()).append("\n");
        
        if (report.getPreProcessingScore() != null) {
            prompt.append("Pre-processing Score: ").append(String.format("%.2f", report.getPreProcessingScore().getOverallScore() * 100)).append("%\n");
        }
        
        if (report.getPostProcessingScore() != null) {
            prompt.append("Post-processing Score: ").append(String.format("%.2f", report.getPostProcessingScore().getOverallScore() * 100)).append("%\n");
        }
        
        prompt.append("\nData Quality Results:\n");
        if (report.getResults() != null) {
            for (DataQualityResult result : report.getResults()) {
                prompt.append("- ").append(result.getMetric().getDisplayName())
                      .append(": ").append(String.format("%.2f", result.getScore() * 100))
                      .append("% (").append(result.isPassed() ? "PASSED" : "FAILED").append(")\n");
            }
        }
        
        prompt.append("\nPlease provide a concise summary highlighting key findings, improvements made, and overall data quality status.");
        
        return prompt.toString();
    }
    
    private String buildRecommendationsPrompt(List<DataQualityResult> results) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following data quality analysis results, provide specific recommendations for improvement:\n\n");
        
        for (DataQualityResult result : results) {
            prompt.append("Metric: ").append(result.getMetric().getDisplayName()).append("\n");
            prompt.append("Score: ").append(String.format("%.2f", result.getScore() * 100)).append("%\n");
            prompt.append("Status: ").append(result.isPassed() ? "PASSED" : "FAILED").append("\n");
            
            if (result.getIssues() != null && !result.getIssues().isEmpty()) {
                prompt.append("Issues: ").append(String.join(", ", result.getIssues())).append("\n");
            }
            prompt.append("\n");
        }
        
        prompt.append("Please provide 3-5 specific, actionable recommendations to improve data quality. Format as a numbered list.");
        
        return prompt.toString();
    }
    
    private String buildTrendsPrompt(DataQualityReport report) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the data quality trends and patterns from this report:\n\n");
        prompt.append("Dataset: ").append(report.getDatasetName()).append("\n");
        
        if (report.getPreProcessingScore() != null && report.getPostProcessingScore() != null) {
            double improvement = report.getPostProcessingScore().getOverallScore() - report.getPreProcessingScore().getOverallScore();
            prompt.append("Overall Improvement: ").append(String.format("%.2f", improvement * 100)).append(" percentage points\n");
        }
        
        prompt.append("\nProvide insights on data quality patterns, potential root causes of issues, and long-term improvement strategies.");
        
        return prompt.toString();
    }
    
    private String buildUserPrompt(String userPrompt) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Data Quality Expert AI Assistant. Your role is to help users with data quality analysis, recommendations, and best practices.\n\n");
        prompt.append("Context: You are part of a Data Quality Orchestration system that helps organizations analyze and improve their data quality.\n\n");
        prompt.append("User Request: ").append(userPrompt).append("\n\n");
        prompt.append("Please provide a helpful, accurate, and actionable response. If the request is about data analysis, provide specific steps or recommendations. ");
        prompt.append("If it's about data quality metrics, explain them clearly. If it's about best practices, provide practical advice.\n\n");
        prompt.append("Keep your response concise but comprehensive, and format it in a user-friendly way.");
        
        return prompt.toString();
    }
    
    private List<String> parseRecommendations(String response) {
        // Simple parsing - split by numbered list items
        return List.of(response.split("\\d+\\."))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
    
    private String generateFallbackSummary(DataQualityReport report) {
        StringBuilder summary = new StringBuilder();
        summary.append("Data Quality Report Summary for ").append(report.getDatasetName()).append(":\n\n");
        
        if (report.getPreProcessingScore() != null && report.getPostProcessingScore() != null) {
            double improvement = report.getPostProcessingScore().getOverallScore() - report.getPreProcessingScore().getOverallScore();
            summary.append("Overall quality improved by ").append(String.format("%.1f", improvement * 100)).append(" percentage points.\n");
        }
        
        summary.append("Processing completed successfully with automated rectification applied.");
        
        return summary.toString();
    }
    
    private List<String> generateFallbackRecommendations(List<DataQualityResult> results) {
        return List.of(
            "Review data collection processes to improve completeness",
            "Implement data validation rules at the source",
            "Establish regular data quality monitoring",
            "Consider automated data cleansing procedures",
            "Train data entry personnel on quality standards"
        );
    }
}