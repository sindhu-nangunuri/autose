package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class UniquenessAgent extends BaseDataQualityAgent {

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.UNIQUENESS;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing uniqueness for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 1.0, issues, recommendations, details);
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnUniqueness = new HashMap<>();
        Map<String, List<Object>> duplicates = new HashMap<>();
        double totalUniqueness = 0.0;

        for (String column : dataset.getColumns()) {
            Set<Object> uniqueValues = new HashSet<>();
            Map<Object, Long> valueFrequency = new HashMap<>();
            
            for (Map<String, Object> row : dataset.getData()) {
                Object value = row.get(column);
                if (!isBlank(value)) {
                    uniqueValues.add(value);
                    valueFrequency.put(value, valueFrequency.getOrDefault(value, 0L) + 1);
                }
            }
            
            double uniqueness = (double) uniqueValues.size() / totalRows;
            columnUniqueness.put(column, uniqueness);
            totalUniqueness += uniqueness;
            
            // Find duplicates
            List<Object> columnDuplicates = valueFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            if (!columnDuplicates.isEmpty()) {
                duplicates.put(column, columnDuplicates);
            }
            
            if (uniqueness < uniquenessThreshold) {
                issues.add(String.format("Column '%s' has low uniqueness: %.2f%% (%d duplicates)", 
                    column, uniqueness * 100, columnDuplicates.size()));
                recommendations.add(String.format("Review and remove duplicate values in column '%s'", column));
            }
        }

        double overallUniqueness = totalUniqueness / dataset.getColumns().size();
        
        details.put("totalRows", totalRows);
        details.put("uniquenessPerColumn", columnUniqueness);
        details.put("duplicates", duplicates);
        details.put("overallUniqueness", overallUniqueness);

        return createResult(getMetric(), overallUniqueness, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying uniqueness issues for dataset: " + dataset.getName());
        
        if (result.getDetails() == null) {
            return dataset;
        }

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> duplicates = (Map<String, List<Object>>) result.getDetails().get("duplicates");
        
        if (duplicates == null || duplicates.isEmpty()) {
            return dataset;
        }

        // Remove duplicate rows based on key columns
        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        Set<String> seenRows = new HashSet<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            // Create a key based on all column values
            String rowKey = dataset.getColumns().stream()
                .map(col -> String.valueOf(row.get(col)))
                .collect(Collectors.joining("|"));
            
            if (!seenRows.contains(rowKey)) {
                seenRows.add(rowKey);
                rectifiedData.add(new HashMap<>(row));
            }
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        logger.info(String.format("Removed %d duplicate rows from dataset", 
            dataset.getData().size() - rectifiedData.size()));
        
        return rectifiedDataset;
    }
}