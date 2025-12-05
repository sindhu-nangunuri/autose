package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityMetric;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

@ApplicationScoped
public class OutliersAgent extends BaseDataQualityAgent {

    @Override
    public DataQualityMetric getMetric() {
        return DataQualityMetric.OUTLIERS;
    }

    @Override
    public DataQualityResult analyze(Dataset dataset) {
        logger.info("Analyzing outliers for dataset: " + dataset.getName());
        
        List<String> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();
        
        if (dataset.getData() == null || dataset.getData().isEmpty()) {
            return createResult(getMetric(), 1.0, issues, recommendations, details);
        }

        int totalRows = dataset.getData().size();
        Map<String, Double> columnOutlierRates = new HashMap<>();
        Map<String, List<Object>> outliers = new HashMap<>();
        double totalOutlierRate = 0.0;
        int numericColumns = 0;

        for (String column : dataset.getColumns()) {
            List<Double> numericValues = new ArrayList<>();
            
            // Extract numeric values
            for (Map<String, Object> row : dataset.getData()) {
                Object value = row.get(column);
                if (!isBlank(value) && isNumeric(value.toString())) {
                    numericValues.add(Double.parseDouble(value.toString()));
                }
            }
            
            if (numericValues.size() < 3) {
                // Skip non-numeric or insufficient data columns
                continue;
            }
            
            numericColumns++;
            List<Object> columnOutliers = detectOutliers(numericValues);
            double outlierRate = (double) columnOutliers.size() / totalRows;
            
            columnOutlierRates.put(column, outlierRate);
            totalOutlierRate += outlierRate;
            
            if (!columnOutliers.isEmpty()) {
                outliers.put(column, columnOutliers);
            }
            
            if (outlierRate > outliersThreshold) {
                issues.add(String.format("Column '%s' has high outlier rate: %.2f%% (%d outliers)", 
                    column, outlierRate * 100, columnOutliers.size()));
                recommendations.add(String.format("Review outliers in column '%s' - consider data validation or transformation", column));
            }
        }

        double overallOutlierRate = numericColumns > 0 ? totalOutlierRate / numericColumns : 0.0;
        double score = Math.max(0.0, 1.0 - overallOutlierRate); // Higher score = fewer outliers
        
        details.put("totalRows", totalRows);
        details.put("numericColumns", numericColumns);
        details.put("outlierRatesPerColumn", columnOutlierRates);
        details.put("outliers", outliers);
        details.put("overallOutlierRate", overallOutlierRate);

        return createResult(getMetric(), score, issues, recommendations, details);
    }

    @Override
    public Dataset rectify(Dataset dataset, DataQualityResult result) {
        logger.info("Rectifying outliers for dataset: " + dataset.getName());
        
        if (result.getDetails() == null) {
            return dataset;
        }

        @SuppressWarnings("unchecked")
        Map<String, List<Object>> outliers = (Map<String, List<Object>>) result.getDetails().get("outliers");
        
        if (outliers == null || outliers.isEmpty()) {
            return dataset;
        }

        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> newRow = new HashMap<>(row);
            
            for (String column : dataset.getColumns()) {
                Object value = newRow.get(column);
                if (!isBlank(value) && isNumeric(value.toString())) {
                    double numValue = Double.parseDouble(value.toString());
                    
                    // Check if this value is an outlier
                    List<Object> columnOutliers = outliers.get(column);
                    if (columnOutliers != null && columnOutliers.contains(numValue)) {
                        // Replace outlier with median value
                        double medianValue = calculateMedian(dataset, column);
                        newRow.put(column, medianValue);
                    }
                }
            }
            
            rectifiedData.add(newRow);
        }

        Dataset rectifiedDataset = new Dataset(dataset.getId(), dataset.getName(), dataset.getColumns(), rectifiedData);
        rectifiedDataset.setMetadata(dataset.getMetadata());
        
        return rectifiedDataset;
    }

    private List<Object> detectOutliers(List<Double> values) {
        if (values.size() < 3) {
            return new ArrayList<>();
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();
        values.forEach(stats::addValue);
        
        double q1 = stats.getPercentile(25);
        double q3 = stats.getPercentile(75);
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;
        
        List<Object> outliers = new ArrayList<>();
        for (Double value : values) {
            if (value < lowerBound || value > upperBound) {
                outliers.add(value);
            }
        }
        
        return outliers;
    }

    private double calculateMedian(Dataset dataset, String column) {
        List<Double> values = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Object value = row.get(column);
            if (!isBlank(value) && isNumeric(value.toString())) {
                values.add(Double.parseDouble(value.toString()));
            }
        }
        
        if (values.isEmpty()) {
            return 0.0;
        }
        
        Collections.sort(values);
        int size = values.size();
        
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }
}