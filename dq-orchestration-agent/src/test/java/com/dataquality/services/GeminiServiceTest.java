package com.dataquality.services;

import com.dataquality.models.*;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class GeminiServiceTest {

    @Inject
    GeminiService geminiService;

    private List<DataQualityResult> testResults;
    private DataQualityReport testReport;

    @BeforeEach
    void setUp() {
        // Create test results
        testResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.85, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 0.95, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 0.75, 0.8)
        );

        // Set up issues and recommendations
        testResults.get(0).setIssues(Arrays.asList("Missing values in name column"));
        testResults.get(0).setRecommendations(Arrays.asList("Fill missing values with default"));
        
        testResults.get(2).setIssues(Arrays.asList("Invalid email formats"));
        testResults.get(2).setRecommendations(Arrays.asList("Validate email format"));

        // Create test report
        testReport = new DataQualityReport();
        testReport.setDatasetName("Test Dataset");
        testReport.setResults(testResults);
        
        Map<DataQualityMetric, Double> metricScores = new HashMap<>();
        metricScores.put(DataQualityMetric.COMPLETENESS, 0.85);
        metricScores.put(DataQualityMetric.UNIQUENESS, 0.95);
        metricScores.put(DataQualityMetric.VALIDITY, 0.75);
        
        DataQualityScore score = new DataQualityScore(0.85, metricScores);
        testReport.setPreProcessingScore(score);
    }

    @Test
    void testGenerateDataQualitySummary() {
        String summary = geminiService.generateDataQualitySummary(testReport);
        
        assertNotNull(summary);
        assertFalse(summary.trim().isEmpty());
        // Should return either real response or fallback summary
        assertTrue(summary.contains("Test Dataset") || summary.contains("Data Quality Report"));
    }

    @Test
    void testGenerateRecommendations() {
        List<String> recommendations = geminiService.generateRecommendations(testResults);
        
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Should contain actionable recommendations
        assertTrue(recommendations.stream().anyMatch(r -> 
            r.toLowerCase().contains("missing") || 
            r.toLowerCase().contains("fill") ||
            r.toLowerCase().contains("complete")));
    }

    @Test
    void testGenerateDataQualitySummaryWithNullReport() {
        assertThrows(Exception.class, () -> {
            geminiService.generateDataQualitySummary(null);
        });
    }

    @Test
    void testGenerateRecommendationsWithNullResults() {
        assertThrows(Exception.class, () -> {
            geminiService.generateRecommendations(null);
        });
    }

    @Test
    void testGenerateRecommendationsWithEmptyResults() {
        List<String> recommendations = geminiService.generateRecommendations(Arrays.asList());
        
        assertNotNull(recommendations);
        // Should handle empty results gracefully - return fallback recommendations
        assertTrue(!recommendations.isEmpty());
    }

    @Test
    void testGenerateDataQualitySummaryWithHighQualityData() {
        // Create high-quality results
        List<DataQualityResult> highQualityResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.98, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 0.99, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 0.97, 0.8)
        );

        DataQualityReport highQualityReport = new DataQualityReport();
        highQualityReport.setDatasetName("High Quality Dataset");
        highQualityReport.setResults(highQualityResults);
        
        Map<DataQualityMetric, Double> metricScores = new HashMap<>();
        metricScores.put(DataQualityMetric.COMPLETENESS, 0.98);
        metricScores.put(DataQualityMetric.UNIQUENESS, 0.99);
        metricScores.put(DataQualityMetric.VALIDITY, 0.97);
        
        DataQualityScore score = new DataQualityScore(0.98, metricScores);
        highQualityReport.setPreProcessingScore(score);

        String summary = geminiService.generateDataQualitySummary(highQualityReport);
        
        assertNotNull(summary);
        assertFalse(summary.trim().isEmpty());
        
        // Should reflect high quality
        assertTrue(summary.toLowerCase().contains("high") || 
                  summary.toLowerCase().contains("excellent") ||
                  summary.toLowerCase().contains("good") ||
                  summary.contains("A"));
    }

    @Test
    void testGenerateRecommendationsWithNoIssues() {
        // Create results with no issues
        List<DataQualityResult> perfectResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 1.0, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 1.0, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 1.0, 0.8)
        );

        List<String> recommendations = geminiService.generateRecommendations(perfectResults);
        
        assertNotNull(recommendations);
        
        // Should provide maintenance recommendations even for perfect data
        if (!recommendations.isEmpty()) {
            assertTrue(recommendations.stream().anyMatch(r -> 
                r.toLowerCase().contains("maintain") || 
                r.toLowerCase().contains("monitor") ||
                r.toLowerCase().contains("continue")));
        }
    }

    @Test
    void testServiceAvailability() {
        // Test that the service is properly injected and available
        assertNotNull(geminiService);
        
        // Test basic functionality
        assertDoesNotThrow(() -> {
            geminiService.generateDataQualitySummary(testReport);
            geminiService.generateRecommendations(testResults);
        });
    }
}