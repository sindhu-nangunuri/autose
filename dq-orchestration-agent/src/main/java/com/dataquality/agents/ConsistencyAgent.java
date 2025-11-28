package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class ConsistencyAgent extends BaseDataQualityAgent {

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.CONSISTENCY;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing consistency for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 1.0, issues, recommendations, details);
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnConsistency = new HashMap<>();
        Map<String, List<String>> inconsistencies = new HashMap<>();
        double totalConsistency = 0.0;

        for (String column : dataset.getColumns()) {
            double consistency = analyzeColumnConsistency(dataset, column, inconsistencies);
            columnConsistency.put(column, consistency);
            totalConsistency += consistency;
            
            if (consistency < consistencyThreshold) {
                issues.add(String.format("Column '%s' has low consistency: %.2f%%", column, consistency * 100));
                recommendations.add(String.format("Standardize data formats and values in column '%s'", column));
            }
        }

        double overallConsistency = totalConsistency / dataset.getColumns().size();
        
        details.put("totalRows", totalRows);
        details.put("consistencyPerColumn", columnConsistency);
        details.put("inconsistencies", inconsistencies);
        details.put("overallConsistency", overallConsistency);

        return createResult(getMetric(), overallConsistency, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying consistency issues for dataset: " + dataset.getName());
        
        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            for (String column : dataset.getColumns()) {
                Object value = newRow.get(column);
                if (!isBlank(value)) {
                    Object standardizedValue = standardizeValue(column, value.toString());
                    newRow.put(column, standardizedValue);
                }
            }
            
            rectifiedData.add(newRow);
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        return rectifiedDataset;
    }

    private double analyzeColumnConsistency(Dataset dataset, String column, Map<String, List<String>> inconsistencies) {
        Map<String, Integer> formatCounts = new HashMap<>();
        List<String> columnInconsistencies = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Object value = row.get(column);
            if (!isBlank(value)) {
                String format = detectFormat(value.toString());
                formatCounts.put(format, formatCounts.getOrDefault(format, 0) + 1);
            }
        }
        
        if (formatCounts.isEmpty()) {
            return 1.0;
        }
        
        // Find the most common format
        String dominantFormat = formatCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
        
        int dominantCount = formatCounts.get(dominantFormat);
        int totalValues = formatCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        // Add inconsistent formats to the list
        for (Map.Entry<String, Integer> entry : formatCounts.entrySet()) {
            if (!entry.getKey().equals(dominantFormat)) {
                columnInconsistencies.add(String.format("%s format (%d occurrences)", entry.getKey(), entry.getValue()));
            }
        }
        
        if (!columnInconsistencies.isEmpty()) {
            inconsistencies.put(column, columnInconsistencies);
        }
        
        return (double) dominantCount / totalValues;
    }

    private String detectFormat(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "empty";
        }
        
        value = value.trim();
        
        // Date formats
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) return "date_iso";
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) return "date_us";
        if (value.matches("\\d{2}-\\d{2}-\\d{4}")) return "date_eu";
        
        // Phone formats
        if (value.matches("\\+\\d{1,3}\\s\\d{3}\\s\\d{3}\\s\\d{4}")) return "phone_international";
        if (value.matches("\\(\\d{3}\\)\\s\\d{3}-\\d{4}")) return "phone_us";
        if (value.matches("\\d{3}-\\d{3}-\\d{4}")) return "phone_dash";
        if (value.matches("\\d{10}")) return "phone_plain";
        
        // Email formats
        if (value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) return "email";
        
        // Numeric formats
        if (value.matches("\\d+")) return "integer";
        if (value.matches("\\d+\\.\\d+")) return "decimal";
        if (value.matches("\\$\\d+(\\.\\d{2})?")) return "currency";
        
        // Text case formats
        if (value.equals(value.toUpperCase())) return "uppercase";
        if (value.equals(value.toLowerCase())) return "lowercase";
        if (Character.isUpperCase(value.charAt(0))) return "titlecase";
        
        return "mixed";
    }

    private Object standardizeValue(String column, String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        String columnLower = column.toLowerCase();
        value = value.trim();
        
        // Standardize names to title case
        if (columnLower.contains("name")) {
            return toTitleCase(value);
        }
        
        // Standardize emails to lowercase
        if (columnLower.contains("email")) {
            return value.toLowerCase();
        }
        
        // Standardize phone numbers
        if (columnLower.contains("phone") || columnLower.contains("mobile")) {
            return standardizePhone(value);
        }
        
        // Standardize dates to ISO format
        if (columnLower.contains("date")) {
            return standardizeDate(value);
        }
        
        return value;
    }

    private String toTitleCase(String text) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }

    private String standardizePhone(String phone) {
        // Remove all non-digit characters except +
        String cleaned = phone.replaceAll("[^+0-9]", "");
        
        if (cleaned.length() == 10) {
            // Format as (XXX) XXX-XXXX
            return String.format("(%s) %s-%s", 
                cleaned.substring(0, 3),
                cleaned.substring(3, 6),
                cleaned.substring(6));
        } else if (cleaned.length() == 11 && cleaned.startsWith("1")) {
            // US number with country code
            return String.format("+1 (%s) %s-%s", 
                cleaned.substring(1, 4),
                cleaned.substring(4, 7),
                cleaned.substring(7));
        }
        
        return phone; // Return original if can't standardize
    }

    private String standardizeDate(String date) {
        // Convert various date formats to ISO format (YYYY-MM-DD)
        if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            // MM/DD/YYYY to YYYY-MM-DD
            String[] parts = date.split("/");
            return String.format("%s-%s-%s", parts[2], parts[0], parts[1]);
        } else if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            // DD-MM-YYYY to YYYY-MM-DD
            String[] parts = date.split("-");
            return String.format("%s-%s-%s", parts[2], parts[1], parts[0]);
        }
        
        return date; // Return original if already in ISO format or unknown format
    }
}