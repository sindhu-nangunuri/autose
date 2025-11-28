package com.dataquality.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DatasetTest {

    private Dataset dataset;
    private List<String> columns;
    private List<Map<String, Object>> data;
    private Map<String, Object> metadata;

    @BeforeEach
    void setUp() {
        columns = Arrays.asList("id", "name", "email", "age");
        
        data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name", "John Doe");
        row1.put("email", "john@example.com");
        row1.put("age", "30");
        data.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", "Jane Smith");
        row2.put("email", "jane@example.com");
        row2.put("age", "25");
        data.add(row2);
        
        Map<String, Object> row3 = new HashMap<>();
        row3.put("id", "3");
        row3.put("name", "Bob Johnson");
        row3.put("email", "");
        row3.put("age", "35");
        data.add(row3);
        
        metadata = new HashMap<>();
        metadata.put("source", "user_database");
        metadata.put("lastUpdated", "2023-12-01");
        
        dataset = new Dataset();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(dataset);
        assertNull(dataset.getName());
        assertNull(dataset.getColumns());
        assertNull(dataset.getData());
        assertNull(dataset.getMetadata());
        assertNotNull(dataset.getCreatedAt());
        assertEquals(0, dataset.getRowCount());
        assertEquals(0, dataset.getColumnCount());
    }

    @Test
    void testParameterizedConstructor() {
        Dataset paramDataset = new Dataset("test-id", "Test Dataset", columns, data);
        
        assertEquals("test-id", paramDataset.getId());
        assertEquals("Test Dataset", paramDataset.getName());
        assertEquals(columns, paramDataset.getColumns());
        assertEquals(data, paramDataset.getData());
        assertEquals(3, paramDataset.getRowCount());
        assertEquals(4, paramDataset.getColumnCount());
        assertNotNull(paramDataset.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        dataset.setId("test-id");
        assertEquals("test-id", dataset.getId());

        dataset.setName("Test Dataset");
        assertEquals("Test Dataset", dataset.getName());

        dataset.setColumns(columns);
        assertEquals(columns, dataset.getColumns());
        assertEquals(4, dataset.getColumnCount());

        dataset.setData(data);
        assertEquals(data, dataset.getData());
        assertEquals(3, dataset.getRowCount());

        dataset.setMetadata(metadata);
        assertEquals(metadata, dataset.getMetadata());
    }

    @Test
    void testDatasetWithCompleteData() {
        dataset.setName("User Data");
        dataset.setColumns(columns);
        dataset.setData(data);
        dataset.setMetadata(metadata);

        assertEquals("User Data", dataset.getName());
        assertEquals(4, dataset.getColumns().size());
        assertEquals(3, dataset.getData().size());
        assertEquals(2, dataset.getMetadata().size());
        
        assertTrue(dataset.getColumns().contains("id"));
        assertTrue(dataset.getColumns().contains("name"));
        assertTrue(dataset.getColumns().contains("email"));
        assertTrue(dataset.getColumns().contains("age"));
    }

    @Test
    void testEmptyDataset() {
        dataset.setName("Empty Dataset");
        dataset.setColumns(new ArrayList<>());
        dataset.setData(new ArrayList<>());
        dataset.setMetadata(new HashMap<>());

        assertEquals("Empty Dataset", dataset.getName());
        assertTrue(dataset.getColumns().isEmpty());
        assertTrue(dataset.getData().isEmpty());
        assertTrue(dataset.getMetadata().isEmpty());
        assertEquals(0, dataset.getRowCount());
        assertEquals(0, dataset.getColumnCount());
    }

    @Test
    void testNullValues() {
        dataset.setName(null);
        assertNull(dataset.getName());

        dataset.setColumns(null);
        assertNull(dataset.getColumns());
        assertEquals(0, dataset.getColumnCount());

        dataset.setData(null);
        assertNull(dataset.getData());
        assertEquals(0, dataset.getRowCount());

        dataset.setMetadata(null);
        assertNull(dataset.getMetadata());
    }

    @Test
    void testRowAndColumnCounts() {
        dataset.setColumns(columns);
        dataset.setData(data);

        assertEquals(4, dataset.getColumnCount());
        assertEquals(3, dataset.getRowCount());

        // Test manual count setting
        dataset.setRowCount(10);
        assertEquals(10, dataset.getRowCount());

        dataset.setColumnCount(5);
        assertEquals(5, dataset.getColumnCount());
    }

    @Test
    void testDataIntegrity() {
        dataset.setColumns(columns);
        dataset.setData(data);

        // Verify that data structure is consistent
        for (Map<String, Object> row : dataset.getData()) {
            assertNotNull(row);
            assertTrue(row.containsKey("id"));
            assertTrue(row.containsKey("name"));
            assertTrue(row.containsKey("email"));
            assertTrue(row.containsKey("age"));
        }
    }

    @Test
    void testMetadataOperations() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("rowCount", 100);
        meta.put("columnCount", 5);
        meta.put("hasNulls", true);
        
        dataset.setMetadata(meta);
        
        assertEquals(100, dataset.getMetadata().get("rowCount"));
        assertEquals(5, dataset.getMetadata().get("columnCount"));
        assertEquals(true, dataset.getMetadata().get("hasNulls"));
    }

    @Test
    void testLargeDataset() {
        List<String> largeColumns = Arrays.asList("col1", "col2", "col3", "col4", "col5");
        List<Map<String, Object>> largeData = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("col1", "val" + (i * 5 + 1));
            row.put("col2", "val" + (i * 5 + 2));
            row.put("col3", "val" + (i * 5 + 3));
            row.put("col4", "val" + (i * 5 + 4));
            row.put("col5", "val" + (i * 5 + 5));
            largeData.add(row);
        }

        dataset.setName("Large Dataset");
        dataset.setColumns(largeColumns);
        dataset.setData(largeData);

        assertEquals("Large Dataset", dataset.getName());
        assertEquals(5, dataset.getColumns().size());
        assertEquals(4, dataset.getData().size());
        assertEquals(5, dataset.getColumnCount());
        assertEquals(4, dataset.getRowCount());
    }

    @Test
    void testSpecialCharacters() {
        List<String> specialColumns = Arrays.asList("id", "name with spaces", "email@domain", "age-years");
        List<Map<String, Object>> specialData = new ArrayList<>();
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "1");
        row1.put("name with spaces", "John O'Connor");
        row1.put("email@domain", "john+test@example.com");
        row1.put("age-years", "30");
        specialData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name with spaces", "Jane-Smith");
        row2.put("email@domain", "jane.smith@example.co.uk");
        row2.put("age-years", "25");
        specialData.add(row2);

        dataset.setName("Special Characters Dataset");
        dataset.setColumns(specialColumns);
        dataset.setData(specialData);

        assertEquals("Special Characters Dataset", dataset.getName());
        assertTrue(dataset.getColumns().contains("name with spaces"));
        assertTrue(dataset.getColumns().contains("email@domain"));
        assertEquals("John O'Connor", dataset.getData().get(0).get("name with spaces"));
        assertEquals("john+test@example.com", dataset.getData().get(0).get("email@domain"));
    }
}