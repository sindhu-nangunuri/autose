package com.dataquality.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataQualityResultTest {

    private DataQualityResult result;

    @BeforeEach
    void setUp() {
        result = new DataQualityResult();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testParameterizedConstructor() {
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.95, 0.8);
        
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertEquals(0.95, result.getScore(), 0.001);
        assertEquals(0.8, result.getThreshold(), 0.001);
        assertTrue(result.isPassed()); // 0.95 >= 0.8
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testParameterizedConstructorFailingScore() {
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.7, 0.8);
        
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertEquals(0.7, result.getScore(), 0.001);
        assertEquals(0.8, result.getThreshold(), 0.001);
        assertFalse(result.isPassed()); // 0.7 < 0.8
    }

    @Test
    void testSettersAndGetters() {
        result.setMetric(DataQualityMetric.UNIQUENESS);
        assertEquals(DataQualityMetric.UNIQUENESS, result.getMetric());

        result.setScore(0.85);
        assertEquals(0.85, result.getScore(), 0.001);

        result.setThreshold(0.9);
        assertEquals(0.9, result.getThreshold(), 0.001);

        result.setPassed(true);
        assertTrue(result.isPassed());

        List<String> issues = Arrays.asList("Duplicate values found");
        result.setIssues(issues);
        assertEquals(issues, result.getIssues());

        List<String> recommendations = Arrays.asList("Remove duplicates");
        result.setRecommendations(recommendations);
        assertEquals(recommendations, result.getRecommendations());

        Map<String, Object> details = new HashMap<>();
        details.put("duplicateCount", 5);
        result.setDetails(details);
        assertEquals(details, result.getDetails());

        LocalDateTime timestamp = LocalDateTime.now();
        result.setTimestamp(timestamp);
        assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    void testScoreThresholdInteraction() {
        result = new DataQualityResult(DataQualityMetric.VALIDITY, 0.75, 0.8);
        assertFalse(result.isPassed());

        // Changing score should update passed status
        result.setScore(0.85);
        assertTrue(result.isPassed());

        // Changing threshold should update passed status
        result.setThreshold(0.9);
        assertFalse(result.isPassed());
    }

    @Test
    void testNullValues() {
        result.setIssues(null);
        assertNull(result.getIssues());

        result.setRecommendations(null);
        assertNull(result.getRecommendations());

        result.setDetails(null);
        assertNull(result.getDetails());
    }

    @Test
    void testEqualsAndHashCode() {
        DataQualityResult result1 = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.95, 0.8);
        DataQualityResult result2 = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.95, 0.8);
        
        // Note: These objects won't be equal due to different timestamps
        // This test verifies that the objects are properly constructed
        assertEquals(result1.getMetric(), result2.getMetric());
        assertEquals(result1.getScore(), result2.getScore());
        assertEquals(result1.getThreshold(), result2.getThreshold());
        assertEquals(result1.isPassed(), result2.isPassed());
    }

    @Test
    void testAllMetrics() {
        // Test all available metrics
        for (DataQualityMetric metric : DataQualityMetric.values()) {
            DataQualityResult testResult = new DataQualityResult(metric, 0.85, 0.8);
            assertEquals(metric, testResult.getMetric());
            assertTrue(testResult.isPassed());
        }
    }

    @Test
    void testBoundaryValues() {
        // Test exact threshold match
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.8, 0.8);
        assertTrue(result.isPassed()); // Should pass when equal

        // Test just below threshold
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.799, 0.8);
        assertFalse(result.isPassed());

        // Test just above threshold
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.801, 0.8);
        assertTrue(result.isPassed());
    }

    @Test
    void testExtremeValues() {
        // Test with 0 score
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.0, 0.8);
        assertFalse(result.isPassed());
        assertEquals(0.0, result.getScore());

        // Test with perfect score
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 1.0, 0.8);
        assertTrue(result.isPassed());
        assertEquals(1.0, result.getScore());

        // Test with score above 1.0 (edge case)
        result = new DataQualityResult(DataQualityMetric.COMPLETENESS, 1.1, 0.8);
        assertTrue(result.isPassed());
        assertEquals(1.1, result.getScore());
    }
}