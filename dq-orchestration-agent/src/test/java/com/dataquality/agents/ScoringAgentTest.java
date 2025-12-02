package com.dataquality.agents;

import com.dataquality.models.DataQualityMetric;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityScore;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ScoringAgentTest {

    @Inject
    ScoringAgent scoringAgent;

    private List<DataQualityResult> testResults;

    @BeforeEach
    void setUp() {
        testResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.95, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 0.88, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 0.92, 0.8)
        );
    }

    @Test
    void testCalculateScore() {
        DataQualityScore score = scoringAgent.calculateScore(testResults);

        assertNotNull(score);
        assertTrue(score.getOverallScore() >= 0.0 && score.getOverallScore() <= 1.0);
        assertNotNull(score.getMetricScores());
        assertNotNull(score.getGrade());
        
        // Should have scores for each metric
        assertEquals(3, score.getMetricScores().size());
        assertTrue(score.getMetricScores().containsKey(DataQualityMetric.COMPLETENESS));
        assertTrue(score.getMetricScores().containsKey(DataQualityMetric.UNIQUENESS));
        assertTrue(score.getMetricScores().containsKey(DataQualityMetric.VALIDITY));
    }

    @Test
    void testCalculateScoreWithEmptyResults() {
        DataQualityScore score = scoringAgent.calculateScore(Arrays.asList());

        assertNotNull(score);
        // Should handle empty results gracefully
        assertTrue(score.getOverallScore() >= 0.0 && score.getOverallScore() <= 1.0);
        assertNotNull(score.getGrade());
    }

    @Test
    void testCalculateScoreWithNullResults() {
        assertThrows(Exception.class, () -> {
            scoringAgent.calculateScore(null);
        });
    }

    @Test
    void testCalculateScoreWithPerfectResults() {
        List<DataQualityResult> perfectResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 1.0, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 1.0, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 1.0, 0.8)
        );

        DataQualityScore score = scoringAgent.calculateScore(perfectResults);

        assertNotNull(score);
        assertEquals(1.0, score.getOverallScore(), 0.001);
        assertEquals("A+", score.getGrade());
    }

    @Test
    void testCalculateScoreWithPoorResults() {
        List<DataQualityResult> poorResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.5, 0.8),
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 0.4, 0.9),
            new DataQualityResult(DataQualityMetric.VALIDITY, 0.3, 0.8)
        );

        DataQualityScore score = scoringAgent.calculateScore(poorResults);

        assertNotNull(score);
        assertTrue(score.getOverallScore() < 0.6);
        assertTrue(score.getGrade().equals("F") || score.getGrade().equals("D"));
    }

    @Test
    void testCalculateScoreWithMixedResults() {
        List<DataQualityResult> mixedResults = Arrays.asList(
            new DataQualityResult(DataQualityMetric.COMPLETENESS, 0.95, 0.8), // Good
            new DataQualityResult(DataQualityMetric.UNIQUENESS, 0.6, 0.9),    // Poor
            new DataQualityResult(DataQualityMetric.VALIDITY, 0.85, 0.8)      // Good
        );

        DataQualityScore score = scoringAgent.calculateScore(mixedResults);

        assertNotNull(score);
        // Overall score should be somewhere in the middle
        assertTrue(score.getOverallScore() > 0.6 && score.getOverallScore() < 0.95);
        
        // Should reflect the mixed quality
        assertTrue(score.getGrade().equals("B") || score.getGrade().equals("B+") || 
                  score.getGrade().equals("C+") || score.getGrade().equals("C"));
    }

    @Test
    void testScoreConsistency() {
        // Same input should produce same output
        DataQualityScore score1 = scoringAgent.calculateScore(testResults);
        DataQualityScore score2 = scoringAgent.calculateScore(testResults);

        assertEquals(score1.getOverallScore(), score2.getOverallScore(), 0.001);
        assertEquals(score1.getGrade(), score2.getGrade());
        assertEquals(score1.getMetricScores().size(), score2.getMetricScores().size());
    }

    @Test
    void testAgentAvailability() {
        assertNotNull(scoringAgent);
    }
}