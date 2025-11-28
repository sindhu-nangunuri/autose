package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class BlanksAgent extends BaseDataQualityAgent {

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.BLANKS;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing blanks for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 1.0, issues, recommendations, details);
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnBlankRates = new HashMap<>();
        Map<String, Integer> blankCounts = new HashMap<>();
        double totalBlankRate = 0.0;

        for (String column : dataset.getColumns()) {
            int blankCount = 0;
            
            for (Map<String, Object> row : dataset.getData()) {
                Object value = row.get(column);
                if (isBlank(value) || isEffectivelyBlank(value)) {
                    blankCount++;
                }
            }
            
            double blankRate = (double) blankCount / totalRows;
            columnBlankRates.put(column, blankRate);
            blankCounts.put(column, blankCount);
            totalBlankRate += blankRate;
            
            if (blankRate > blanksThreshold) {
                issues.add(String.format("Column '%s' has high blank rate: %.2f%% (%d blanks)", 
                    column, blankRate * 100, blankCount));
                recommendations.add(String.format("Reduce blank values in column '%s' through better data collection", column));
            }
        }

        double overallBlankRate = totalBlankRate / dataset.getColumns().size();
        double score = Math.max(0.0, 1.0 - overallBlankRate); // Higher score = fewer blanks
        
        details.put("totalRows", totalRows);
        details.put("blankRatesPerColumn", columnBlankRates);
        details.put("blankCounts", blankCounts);
        details.put("overallBlankRate", overallBlankRate);

        return createResult(getMetric(), score, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying blank values for dataset: " + dataset.getName());
        
        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            for (String column : dataset.getColumns()) {
                Object value = newRow.get(column);
                if (isBlank(value) || isEffectivelyBlank(value)) {
                    Object replacementValue = getReplacementValue(dataset, column);
                    newRow.put(column, replacementValue);
                }
            }
            
            rectifiedData.add(newRow);
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        return rectifiedDataset;
    }

    private boolean isEffectivelyBlank(Object value) {
        if (value == null) {
            return true;
        }
        
        String strValue = value.toString().trim();
        
        // Check for various representations of blank/missing values
        return strValue.isEmpty() ||
               strValue.equalsIgnoreCase("null") ||
               strValue.equalsIgnoreCase("n/a") ||
               strValue.equalsIgnoreCase("na") ||
               strValue.equalsIgnoreCase("none") ||
               strValue.equalsIgnoreCase("unknown") ||
               strValue.equalsIgnoreCase("missing") ||
               strValue.equals("-") ||
               strValue.equals("--") ||
               strValue.equals("?") ||
               strValue.equals("???");
    }

    private Object getReplacementValue(Dataset dataset, String column) {
        String columnLower = column.toLowerCase();
        
        // Get non-blank values for this column
        List<Object> nonBlankValues = new ArrayList<>();
        List<Double> numericValues = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Object value = row.get(column);
            if (!isBlank(value) && !isEffectivelyBlank(value)) {
                nonBlankValues.add(value);
                
                if (isNumeric(value.toString())) {
                    numericValues.add(Double.parseDouble(value.toString()));
                }
            }
        }
        
        if (nonBlankValues.isEmpty()) {
            return getDefaultValue(column);
        }
        
        // For numeric columns, use median
        if (!numericValues.isEmpty() && numericValues.size() > nonBlankValues.size() * 0.5) {
            Collections.sort(numericValues);
            int size = numericValues.size();
            if (size % 2 == 0) {
                return (numericValues.get(size / 2 - 1) + numericValues.get(size / 2)) / 2.0;
            } else {
                return numericValues.get(size / 2);
            }
        }
        
        // For categorical columns, use mode (most frequent value)
        Map<Object, Long> frequency = new HashMap<>();
        for (Object value : nonBlankValues) {
            frequency.put(value, frequency.getOrDefault(value, 0L) + 1);
        }
        
        return frequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(getDefaultValue(column));
    }

    private Object getDefaultValue(String column) {
        String columnLower = column.toLowerCase();
        
        // Provide sensible defaults based on column name
        if (columnLower.contains("name")) {
            return "Unknown";
        } else if (columnLower.contains("email")) {
            return "unknown@example.com";
        } else if (columnLower.contains("phone")) {
            return "000-000-0000";
        } else if (columnLower.contains("age")) {
            return 0;
        } else if (columnLower.contains("salary") || columnLower.contains("amount") || columnLower.contains("price")) {
            return 0.0;
        } else if (columnLower.contains("date")) {
            return "1900-01-01";
        } else if (columnLower.contains("id")) {
            return 0;
        } else if (columnLower.contains("count") || columnLower.contains("number")) {
            return 0;
        }
        
        return "Unknown";
    }
}