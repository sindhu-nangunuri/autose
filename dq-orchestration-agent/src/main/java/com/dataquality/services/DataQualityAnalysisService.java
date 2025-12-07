package com.dataquality.services;

import com.dataquality.agents.*;
import com.dataquality.models.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DataQualityAnalysisService {
    
    private static final Logger logger = Logger.getLogger(DataQualityAnalysisService.class);
    
    @Inject
    CompletenessAgent completenessAgent;
    
    @Inject
    UniquenessAgent uniquenessAgent;
    
    @Inject
    ValidityAgent validityAgent;
    
    @Inject
    OutliersAgent outliersAgent;
    
    @Inject
    ConsistencyAgent consistencyAgent;
    
    @Inject
    BlanksAgent blanksAgent;
    
    @Inject
    ScoringAgent scoringAgent;
    
    @Inject
    GeminiService geminiService;
    
    /**
     * Process a dataset and generate a complete data quality report
     */
    public DataQualityReport processDataset(Dataset dataset) {
        logger.info("Processing dataset for data quality analysis: " + dataset.getName());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Analyze data quality
            List<DataQualityResult> preProcessingResults = analyzeDataQuality(dataset);
            DataQualityScore preProcessingScore = calculateDataQualityScore(dataset);
            
            // Apply rectification
            Dataset rectifiedDataset = applyRectification(dataset, preProcessingResults);
            
            // Re-analyze after rectification
            List<DataQualityResult> postProcessingResults = analyzeDataQuality(rectifiedDataset);
            DataQualityScore postProcessingScore = calculateDataQualityScore(rectifiedDataset);
            
            // Generate rectification actions summary
            List<String> rectificationActions = generateRectificationActions(preProcessingResults, postProcessingResults);
            
            // Create report
            DataQualityReport report = new DataQualityReport();
            report.setId(UUID.randomUUID().toString());
            report.setDatasetName(dataset.getName());
            report.setResults(postProcessingResults);
            report.setPreProcessingScore(preProcessingScore);
            report.setPostProcessingScore(postProcessingScore);
            report.setRectificationActions(rectificationActions);
            report.setTimestamp(LocalDateTime.now());
            report.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            
            // Generate AI summary
            try {
                String summary = geminiService.generateDataQualitySummary(report);
                report.setSummary(summary);
            } catch (Exception e) {
                logger.warn("Failed to generate AI summary", e);
                report.setSummary("Data quality analysis completed successfully.");
            }
            
            logger.info("Data quality processing completed for dataset: " + dataset.getName());
            return report;
            
        } catch (Exception e) {
            logger.error("Error processing dataset: " + dataset.getName(), e);
            throw new RuntimeException("Data quality processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analyze data quality without rectification
     */
    public List<DataQualityResult> analyzeDataQuality(Dataset dataset) {
        logger.info("Analyzing data quality for dataset: " + dataset.getName());
        
        List<DataQualityResult> results = new ArrayList<>();
        
        try {
            // Run all quality checks
            results.add(completenessAgent.analyze(dataset));
            results.add(uniquenessAgent.analyze(dataset));
            results.add(validityAgent.analyze(dataset));
            results.add(outliersAgent.analyze(dataset));
            results.add(consistencyAgent.analyze(dataset));
            results.add(blanksAgent.analyze(dataset));
            
            logger.info("Data quality analysis completed with " + results.size() + " metrics");
            return results;
            
        } catch (Exception e) {
            logger.error("Error analyzing data quality", e);
            throw new RuntimeException("Data quality analysis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate overall data quality score
     */
    public DataQualityScore calculateDataQualityScore(Dataset dataset) {
        logger.info("Calculating data quality score for dataset: " + dataset.getName());
        
        try {
            return scoringAgent.calculateScore(dataset);
        } catch (Exception e) {
            logger.error("Error calculating data quality score", e);
            throw new RuntimeException("Score calculation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate recommendations based on analysis results
     */
    public List<String> generateRecommendations(List<DataQualityResult> results) {
        logger.info("Generating recommendations for " + results.size() + " results");
        
        try {
            return geminiService.generateRecommendations(results);
        } catch (Exception e) {
            logger.error("Error generating recommendations", e);
            return generateFallbackRecommendations(results);
        }
    }
    
    /**
     * Apply rectification to the dataset
     */
    private Dataset applyRectification(Dataset dataset, List<DataQualityResult> results) {
        logger.info("Applying rectification to dataset: " + dataset.getName());
        
        // Create a copy of the dataset for rectification
        Dataset rectifiedDataset = new Dataset(
            dataset.getId(),
            dataset.getName(),
            new ArrayList<>(dataset.getColumns()),
            new ArrayList<>()
        );
        
        // Copy metadata
        if (dataset.getMetadata() != null) {
            rectifiedDataset.setMetadata(new HashMap<>(dataset.getMetadata()));
        }
        
        // Apply rectification logic based on the results
        List<Map<String, Object>> rectifiedData = new ArrayList<>();
        
        for (Map<String, Object> row : dataset.getData()) {
            Map<String, Object> rectifiedRow = new HashMap<>(row);
            
            // Apply basic rectification rules
            for (String column : dataset.getColumns()) {
                Object value = rectifiedRow.get(column);
                
                // Handle null/empty values
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    // For now, keep as null - in production, you might want more sophisticated imputation
                    rectifiedRow.put(column, null);
                }
                
                // Handle string trimming
                if (value instanceof String) {
                    rectifiedRow.put(column, ((String) value).trim());
                }
            }
            
            rectifiedData.add(rectifiedRow);
        }
        
        rectifiedDataset.setData(rectifiedData);
        
        logger.info("Rectification applied to dataset: " + dataset.getName());
        return rectifiedDataset;
    }
    
    /**
     * Generate rectification actions summary
     */
    private List<String> generateRectificationActions(List<DataQualityResult> preResults, List<DataQualityResult> postResults) {
        List<String> actions = new ArrayList<>();
        
        // Compare pre and post results to identify improvements
        for (int i = 0; i < preResults.size() && i < postResults.size(); i++) {
            DataQualityResult preResult = preResults.get(i);
            DataQualityResult postResult = postResults.get(i);
            
            if (postResult.getScore() > preResult.getScore()) {
                double improvement = (postResult.getScore() - preResult.getScore()) * 100;
                actions.add(String.format("Improved %s by %.1f percentage points", 
                    preResult.getMetric().getDisplayName(), improvement));
            }
        }
        
        if (actions.isEmpty()) {
            actions.add("Applied data cleansing and standardization procedures");
        }
        
        return actions;
    }
    
    /**
     * Generate fallback recommendations
     */
    private List<String> generateFallbackRecommendations(List<DataQualityResult> results) {
        List<String> recommendations = new ArrayList<>();
        
        for (DataQualityResult result : results) {
            if (!result.isPassed()) {
                switch (result.getMetric()) {
                    case COMPLETENESS:
                        recommendations.add("Implement data validation rules to ensure required fields are populated");
                        break;
                    case UNIQUENESS:
                        recommendations.add("Review data collection processes to prevent duplicate entries");
                        break;
                    case VALIDITY:
                        recommendations.add("Establish format validation rules for data entry");
                        break;
                    case OUTLIERS:
                        recommendations.add("Implement outlier detection and review processes");
                        break;
                    case CONSISTENCY:
                        recommendations.add("Standardize data formats and naming conventions");
                        break;
                    case BLANKS:
                        recommendations.add("Address blank value handling in data collection");
                        break;
                    default:
                        recommendations.add("Review and improve data quality processes for " + result.getMetric().getDisplayName());
                }
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Continue monitoring data quality metrics regularly");
        }
        
        return recommendations;
    }
}