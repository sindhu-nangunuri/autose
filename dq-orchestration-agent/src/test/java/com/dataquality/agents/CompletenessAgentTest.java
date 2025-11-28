package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityMetric;
import com.dataquality.models.DataQualityResult;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CompletenessAgentTest {

    @Inject
    CompletenessAgent completenessAgent;

    private Dataset testDataset;

    @BeforeEach
    void setUp() {
        testDataset = new Dataset();
        testDataset.setName("Test Dataset");
        testDataset.setColumns(Arrays.asList("id", "name", "email"));
    }

    @Test
    void testAnalyzeCompleteData() {
        List<Map<String, Object>> completeData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name", "John");
        row1.put("email", "john@example.com");
        completeData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", "Jane");
        row2.put("email", "jane@example.com");
        completeData.add(row2);
        
        Map<String, Object> row3 = new HashMap<>();
        row3.put("id", "3");
        row3.put("name", "Bob");
        row3.put("email", "bob@example.com");
        completeData.add(row3);
        
        testDataset.setData(completeData);

        DataQualityResult result = completenessAgent.analyze(testDataset);

        assertNotNull(result);
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertEquals(1.0, result.getScore(), 0.001); // 100% complete
        assertTrue(result.isPassed());
    }

    @Test
    void testAnalyzeIncompleteData() {
        List<Map<String, Object>> incompleteData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name", "John");
        row1.put("email", "john@example.com");
        incompleteData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", ""); // Missing name
        row2.put("email", "jane@example.com");
        incompleteData.add(row2);
        
        Map<String, Object> row3 = new HashMap<>();
        row3.put("id", "3");
        row3.put("name", "Bob");
        row3.put("email", ""); // Missing email
        incompleteData.add(row3);
        
        testDataset.setData(incompleteData);

        DataQualityResult result = completenessAgent.analyze(testDataset);

        assertNotNull(result);
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertTrue(result.getScore() < 1.0); // Less than 100% complete
        assertTrue(result.getScore() > 0.0); // But not completely empty
    }

    @Test
    void testAnalyzeEmptyDataset() {
        testDataset.setData(new ArrayList<>());

        DataQualityResult result = completenessAgent.analyze(testDataset);

        assertNotNull(result);
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        // Empty dataset should have a defined behavior
        assertTrue(result.getScore() >= 0.0 && result.getScore() <= 1.0);
    }

    @Test
    void testAnalyzeNullValues() {
        List<Map<String, Object>> nullData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name", "John");
        row1.put("email", "john@example.com");
        nullData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", null); // Null value
        row2.put("email", "jane@example.com");
        nullData.add(row2);
        
        Map<String, Object> row3 = new HashMap<>();
        row3.put("id", "3");
        row3.put("name", "Bob");
        row3.put("email", null); // Null value
        nullData.add(row3);
        
        testDataset.setData(nullData);

        DataQualityResult result = completenessAgent.analyze(testDataset);

        assertNotNull(result);
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertTrue(result.getScore() < 1.0); // Should detect null values as incomplete
    }

    @Test
    void testAnalyzeWithNullDataset() {
        assertThrows(Exception.class, () -> {
            completenessAgent.analyze(null);
        });
    }

    @Test
    void testResultProperties() {
        List<Map<String, Object>> testData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name", "John");
        row1.put("email", "john@example.com");
        testData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", "");
        row2.put("email", "jane@example.com");
        testData.add(row2);
        
        testDataset.setData(testData);

        DataQualityResult result = completenessAgent.analyze(testDataset);

        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        assertEquals(DataQualityMetric.COMPLETENESS, result.getMetric());
        assertTrue(result.getScore() >= 0.0 && result.getScore() <= 1.0);
        
        // Should have threshold set
        assertTrue(result.getThreshold() > 0.0);
    }

    @Test
    void testAgentAvailability() {
        assertNotNull(completenessAgent);
    }
}