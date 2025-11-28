package com.dataquality.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataQualityScoreTest {

    private DataQualityScore score;
    private Map<DataQualityMetric, Double> metricScores;

    @BeforeEach
    void setUp() {
        metricScores = new HashMap<>();
        metricScores.put(DataQualityMetric.COMPLETENESS, 0.95);
        metricScores.put(DataQualityMetric.UNIQUENESS, 0.88);
        metricScores.put(DataQualityMetric.VALIDITY, 0.92);
    }

    @Test
    void testDefaultConstructor() {
        score = new DataQualityScore();
        assertNotNull(score);
        assertEquals(0.0, score.getOverallScore());
        assertNull(score.getMetricScores());
        assertNull(score.getGrade());
    }

    @Test
    void testParameterizedConstructor() {
        score = new DataQualityScore(0.92, metricScores);
        
        assertEquals(0.92, score.getOverallScore(), 0.001);
        assertEquals(metricScores, score.getMetricScores());
        assertEquals("A", score.getGrade()); // 0.92 should be grade A
    }

    @Test
    void testGradeCalculation() {
        // Test A+ grade
        score = new DataQualityScore(0.96, metricScores);
        assertEquals("A+", score.getGrade());

        // Test A grade
        score = new DataQualityScore(0.92, metricScores);
        assertEquals("A", score.getGrade());

        // Test B+ grade
        score = new DataQualityScore(0.87, metricScores);
        assertEquals("B+", score.getGrade());

        // Test B grade
        score = new DataQualityScore(0.82, metricScores);
        assertEquals("B", score.getGrade());

        // Test C+ grade
        score = new DataQualityScore(0.77, metricScores);
        assertEquals("C+", score.getGrade());

        // Test C grade
        score = new DataQualityScore(0.72, metricScores);
        assertEquals("C", score.getGrade());

        // Test D+ grade
        score = new DataQualityScore(0.67, metricScores);
        assertEquals("D+", score.getGrade());

        // Test D grade
        score = new DataQualityScore(0.62, metricScores);
        assertEquals("D", score.getGrade());

        // Test F grade
        score = new DataQualityScore(0.55, metricScores);
        assertEquals("F", score.getGrade());
    }

    @Test
    void testSettersAndGetters() {
        score = new DataQualityScore();
        
        score.setOverallScore(0.85);
        assertEquals(0.85, score.getOverallScore(), 0.001);
        assertEquals("B+", score.getGrade()); // Grade should be auto-calculated

        score.setMetricScores(metricScores);
        assertEquals(metricScores, score.getMetricScores());

        score.setGrade("Custom Grade");
        assertEquals("Custom Grade", score.getGrade());
    }

    @Test
    void testOverallScoreUpdate() {
        score = new DataQualityScore(0.75, metricScores);
        assertEquals("C+", score.getGrade());

        // Changing overall score should update grade
        score.setOverallScore(0.95);
        assertEquals("A+", score.getGrade());
    }

    @Test
    void testEdgeCases() {
        // Test with null metric scores
        score = new DataQualityScore(0.85, null);
        assertEquals(0.85, score.getOverallScore());
        assertNull(score.getMetricScores());
        assertEquals("B+", score.getGrade());

        // Test with empty metric scores
        Map<DataQualityMetric, Double> emptyScores = new HashMap<>();
        score = new DataQualityScore(0.90, emptyScores);
        assertEquals(0.90, score.getOverallScore());
        assertEquals(emptyScores, score.getMetricScores());
        assertEquals("A", score.getGrade());
    }

    @Test
    void testBoundaryValues() {
        // Test exact boundary values
        score = new DataQualityScore(0.95, metricScores);
        assertEquals("A+", score.getGrade());

        score = new DataQualityScore(0.90, metricScores);
        assertEquals("A", score.getGrade());

        score = new DataQualityScore(0.60, metricScores);
        assertEquals("D", score.getGrade());

        score = new DataQualityScore(0.59, metricScores);
        assertEquals("F", score.getGrade());
    }

    @Test
    void testNegativeAndExtremeValues() {
        // Test negative score
        score = new DataQualityScore(-0.1, metricScores);
        assertEquals(-0.1, score.getOverallScore());
        assertEquals("F", score.getGrade());

        // Test score above 1.0
        score = new DataQualityScore(1.1, metricScores);
        assertEquals(1.1, score.getOverallScore());
        assertEquals("A+", score.getGrade());

        // Test zero score
        score = new DataQualityScore(0.0, metricScores);
        assertEquals(0.0, score.getOverallScore());
        assertEquals("F", score.getGrade());
    }

    @Test
    void testAllMetricsInScores() {
        Map<DataQualityMetric, Double> allMetrics = new HashMap<>();
        for (DataQualityMetric metric : DataQualityMetric.values()) {
            allMetrics.put(metric, 0.85);
        }

        score = new DataQualityScore(0.85, allMetrics);
        assertEquals(DataQualityMetric.values().length, score.getMetricScores().size());
        assertEquals("B+", score.getGrade());

        // Verify all metrics are present
        for (DataQualityMetric metric : DataQualityMetric.values()) {
            assertTrue(score.getMetricScores().containsKey(metric));
            assertEquals(0.85, score.getMetricScores().get(metric), 0.001);
        }
    }
}