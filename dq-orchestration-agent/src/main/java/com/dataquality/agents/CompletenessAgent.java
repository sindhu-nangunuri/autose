package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class CompletenessAgent extends BaseDataQualityAgent {

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.COMPLETENESS;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing completeness for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 0.0, 
                List.of("Dataset is empty"), 
                List.of("Provide data for analysis"), 
                Map.of("totalRows", 0, "completenessPerColumn", new HashMap<>()));
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnCompleteness = new HashMap<>();
        double totalCompleteness = 0.0;

        for (String column : dataset.getColumns()) {
            long nonNullCount = dataset.getData().stream()
                .mapToLong(row -> isBlank(row.get(column)) ? 0 : 1)
                .sum();
            
            double completeness = (double) nonNullCount / totalRows;
            columnCompleteness.put(column, completeness);
            totalCompleteness += completeness;
            
            if (completeness < completenessThreshold) {
                issues.add(String.format("Column '%s' has low completeness: %.2f%%", column, completeness * 100));
                recommendations.add(String.format("Consider data imputation or collection improvement for column '%s'", column));
            }
        }

        double overallCompleteness = totalCompleteness / dataset.getColumns().size();
        
        details.put("totalRows", totalRows);
        details.put("completenessPerColumn", columnCompleteness);
        details.put("overallCompleteness", overallCompleteness);

        return createResult(getMetric(), overallCompleteness, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying completeness issues for dataset: " + dataset.getName());
        
        if (result.getDetails() == null) {
            return dataset;
        }

        @SuppressWarnings("unchecked")
        Map<String, Double> columnCompleteness = (Map<String, Double>) result.getDetails().get("completenessPerColumn");
        
        if (columnCompleteness == null) {
            return dataset;
        }

        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            for (String column : dataset.getColumns()) {
                if (isBlank(newRow.get(column))) {
                    // Simple imputation strategy - could be enhanced with ML-based approaches
                    Object imputedValue = getImputedValue(dataset, column);
                    newRow.put(column, imputedValue);
                }
            }
            
            rectifiedData.add(newRow);
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        return rectifiedDataset;
    }

    private Object getImputedValue(Dataset dataset, String column) {
        // Get the most common non-null value for categorical data
        // or mean for numerical data
        Map<Object, Long> valueFrequency = new HashMap<>();
        List<Double> numericValues = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Object value = row.get(column);
            if (!isBlank(value)) {
                valueFrequency.put(value, valueFrequency.getOrDefault(value, 0L) + 1);
                
                if (isNumeric(value.toString())) {
                    numericValues.add(Double.parseDouble(value.toString()));
                }
            }
        }

        if (!numericValues.isEmpty()) {
            // Return mean for numeric columns
            return numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        } else if (!valueFrequency.isEmpty()) {
            // Return most frequent value for categorical columns
            return valueFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");
        }
        
        return "UNKNOWN";
    }
}