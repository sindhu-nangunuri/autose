package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.regex.Pattern;

@ApplicationScoped
public class ValidityAgent extends BaseDataQualityAgent {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]?[0-9]{7,15}$");
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}/\\d{2}/\\d{4}$|^\\d{2}-\\d{2}-\\d{4}$");

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.VALIDITY;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing validity for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 1.0, issues, recommendations, details);
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnValidity = new HashMap<>();
        Map<String, List<String>> invalidValues = new HashMap<>();
        double totalValidity = 0.0;

        for (String column : dataset.getColumns()) {
            int validCount = 0;
            List<String> columnInvalidValues = new ArrayList<>();
            
            for (Map<String, Object> row : dataset.getData()) {
                Object value = row.get(column);
                if (!isBlank(value)) {
                    if (isValidValue(column, value.toString())) {
                        validCount++;
                    } else {
                        columnInvalidValues.add(value.toString());
                    }
                }
            }
            
            double validity = (double) validCount / totalRows;
            columnValidity.put(column, validity);
            totalValidity += validity;
            
            if (!columnInvalidValues.isEmpty()) {
                invalidValues.put(column, columnInvalidValues.subList(0, Math.min(10, columnInvalidValues.size())));
            }
            
            if (validity < validityThreshold) {
                issues.add(String.format("Column '%s' has low validity: %.2f%% (%d invalid values)", 
                    column, validity * 100, columnInvalidValues.size()));
                recommendations.add(String.format("Review and correct invalid values in column '%s'", column));
            }
        }

        double overallValidity = totalValidity / dataset.getColumns().size();
        
        details.put("totalRows", totalRows);
        details.put("validityPerColumn", columnValidity);
        details.put("invalidValues", invalidValues);
        details.put("overallValidity", overallValidity);

        return createResult(getMetric(), overallValidity, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying validity issues for dataset: " + dataset.getName());
        
        if (result.getDetails() == null) {
            return dataset;
        }

        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            for (String column : dataset.getColumns()) {
                Object value = newRow.get(column);
                if (!isBlank(value) && !isValidValue(column, value.toString())) {
                    // Attempt to correct common validity issues
                    Object correctedValue = correctInvalidValue(column, value.toString());
                    newRow.put(column, correctedValue);
                }
            }
            
            rectifiedData.add(newRow);
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        return rectifiedDataset;
    }

    private boolean isValidValue(String column, String value) {
        String columnLower = column.toLowerCase();
        
        // Email validation
        if (columnLower.contains("email") || columnLower.contains("mail")) {
            return EMAIL_PATTERN.matcher(value).matches();
        }
        
        // Phone validation
        if (columnLower.contains("phone") || columnLower.contains("mobile") || columnLower.contains("tel")) {
            return PHONE_PATTERN.matcher(value.replaceAll("[\\s()-]", "")).matches();
        }
        
        // Date validation
        if (columnLower.contains("date") || columnLower.contains("time")) {
            return DATE_PATTERN.matcher(value).matches();
        }
        
        // Numeric validation
        if (columnLower.contains("age") || columnLower.contains("count") || 
            columnLower.contains("amount") || columnLower.contains("price")) {
            return isNumeric(value) && Double.parseDouble(value) >= 0;
        }
        
        // Default: non-empty string is valid
        return !value.trim().isEmpty();
    }

    private Object correctInvalidValue(String column, String value) {
        String columnLower = column.toLowerCase();
        
        // Email correction
        if (columnLower.contains("email") || columnLower.contains("mail")) {
            return correctEmail(value);
        }
        
        // Phone correction
        if (columnLower.contains("phone") || columnLower.contains("mobile") || columnLower.contains("tel")) {
            return correctPhone(value);
        }
        
        // Numeric correction
        if (columnLower.contains("age") || columnLower.contains("count") || 
            columnLower.contains("amount") || columnLower.contains("price")) {
            return correctNumeric(value);
        }
        
        // Default: return trimmed value
        return value.trim();
    }

    private String correctEmail(String email) {
        // Basic email correction
        email = email.trim().toLowerCase();
        if (!email.contains("@")) {
            return "invalid@example.com";
        }
        return email;
    }

    private String correctPhone(String phone) {
        // Remove all non-numeric characters except +
        String cleaned = phone.replaceAll("[^+0-9]", "");
        if (cleaned.length() < 7) {
            return "0000000000";
        }
        return cleaned;
    }

    private String correctNumeric(String value) {
        // Extract numeric part
        String numeric = value.replaceAll("[^0-9.-]", "");
        try {
            double num = Double.parseDouble(numeric);
            return String.valueOf(Math.abs(num)); // Ensure positive
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}