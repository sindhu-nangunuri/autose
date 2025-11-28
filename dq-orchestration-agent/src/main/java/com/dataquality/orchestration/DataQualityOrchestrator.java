package com.dataquality.orchestration;

import com.dataquality.agents.*;
import com.dataquality.models.*;
import com.dataquality.services.GeminiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped
public class DataQualityOrchestrator {
    
    private static final Logger logger = Logger.getLogger(DataQualityOrchestrator.class);
    
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
    
    @ConfigProperty(name = "data-quality.processing.max-concurrent-workers", defaultValue = "5")
    int maxConcurrentWorkers;
    
    @ConfigProperty(name = "data-quality.processing.timeout-seconds", defaultValue = "300")
    int timeoutSeconds;
    
    private final ExecutorService executorService;
    private final List<DataQualityAgent> agents;
    
    public DataQualityOrchestrator() {
        this.executorService = Executors.newCachedThreadPool();
        this.agents = new ArrayList<>();
    }
    
    @jakarta.annotation.PostConstruct
    void initializeAgents() {
        agents.add(completenessAgent);
        agents.add(uniquenessAgent);
        agents.add(validityAgent);
        agents.add(outliersAgent);
        agents.add(consistencyAgent);
        agents.add(blanksAgent);
        
        logger.info("Initialized " + agents.size() + " data quality agents");
    }
    
    @jakarta.annotation.PreDestroy
    void cleanup() {
        executorService.shutdown();
    }
    
    public DataQualityReport processDataset(Dataset dataset) {
        logger.info("Starting data quality processing for dataset: " + dataset.getName());
        long startTime = System.currentTimeMillis();
        
        String reportId = UUID.randomUUID().toString();
        DataQualityReport report = new DataQualityReport(reportId, dataset.getName());
        
        try {
            // Step 1: Analyze initial data quality
            logger.info("Step 1: Analyzing initial data quality");
            List<DataQualityResult> initialResults = analyzeDataQuality(dataset);
            DataQualityScore preProcessingScore = scoringAgent.calculateScore(initialResults);
            report.setPreProcessingScore(preProcessingScore);
            
            logger.info(String.format("Initial data quality score: %.2f%% (%s)", 
                preProcessingScore.getOverallScore() * 100, preProcessingScore.getGrade()));
            
            // Step 2: Rectify data quality issues
            logger.info("Step 2: Rectifying data quality issues");
            Dataset rectifiedDataset = rectifyDataset(dataset, initialResults);
            List<String> rectificationActions = extractRectificationActions(initialResults);
            report.setRectificationActions(rectificationActions);
            
            // Step 3: Re-analyze data quality after rectification
            logger.info("Step 3: Re-analyzing data quality after rectification");
            List<DataQualityResult> finalResults = analyzeDataQuality(rectifiedDataset);
            DataQualityScore postProcessingScore = scoringAgent.calculateScore(finalResults);
            report.setPostProcessingScore(postProcessingScore);
            report.setResults(finalResults);
            
            logger.info(String.format("Final data quality score: %.2f%% (%s)", 
                postProcessingScore.getOverallScore() * 100, postProcessingScore.getGrade()));
            
            // Step 4: Generate AI-powered summary and insights
            logger.info("Step 4: Generating AI-powered summary");
            String summary = geminiService.generateDataQualitySummary(report);
            report.setSummary(summary);
            
            long processingTime = System.currentTimeMillis() - startTime;
            report.setProcessingTimeMs(processingTime);
            
            logger.info(String.format("Data quality processing completed in %d ms", processingTime));
            
            return report;
            
        } catch (Exception e) {
            logger.error("Error during data quality processing", e);
            throw new RuntimeException("Data quality processing failed", e);
        }
    }
    
    public List<DataQualityResult> analyzeDataQuality(Dataset dataset) {
        logger.info("Analyzing data quality with " + agents.size() + " agents");
        
        List<CompletableFuture<DataQualityResult>> futures = agents.stream()
            .map(agent -> CompletableFuture.supplyAsync(() -> {
                try {
                    logger.debug("Running agent: " + agent.getAgentName());
                    return agent.analyze(dataset);
                } catch (Exception e) {
                    logger.error("Agent " + agent.getAgentName() + " failed", e);
                    return createErrorResult(agent.getMetric(), e);
                }
            }, executorService))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    public Dataset rectifyDataset(Dataset dataset, List<DataQualityResult> results) {
        logger.info("Rectifying dataset based on analysis results");
        
        Dataset currentDataset = dataset;
        
        // Apply rectifications sequentially to avoid conflicts
        for (DataQualityResult result : results) {
            if (!result.isPassed()) {
                DataQualityAgent agent = findAgentForMetric(result.getMetric());
                if (agent != null) {
                    try {
                        logger.debug("Applying rectification for: " + result.getMetric().getDisplayName());
                        currentDataset = agent.rectify(currentDataset, result);
                    } catch (Exception e) {
                        logger.error("Rectification failed for metric: " + result.getMetric().getDisplayName(), e);
                    }
                }
            }
        }
        
        return currentDataset;
    }
    
    public DataQualityScore calculateDataQualityScore(Dataset dataset) {
        List<DataQualityResult> results = analyzeDataQuality(dataset);
        return scoringAgent.calculateScore(results);
    }
    
    public List<String> generateRecommendations(List<DataQualityResult> results) {
        try {
            return geminiService.generateRecommendations(results);
        } catch (Exception e) {
            logger.error("Failed to generate AI recommendations", e);
            return generateBasicRecommendations(results);
        }
    }
    
    private DataQualityAgent findAgentForMetric(DataQualityMetric metric) {
        return agents.stream()
            .filter(agent -> agent.canHandle(metric))
            .findFirst()
            .orElse(null);
    }
    
    private DataQualityResult createErrorResult(DataQualityMetric metric, Exception e) {
        DataQualityResult result = new DataQualityResult();
        result.setMetric(metric);
        result.setScore(0.0);
        result.setThreshold(0.0);
        result.setPassed(false);
        result.setIssues(List.of("Analysis failed: " + e.getMessage()));
        result.setRecommendations(List.of("Review data format and try again"));
        result.setDetails(Map.of("error", e.getClass().getSimpleName()));
        return result;
    }
    
    private List<String> extractRectificationActions(List<DataQualityResult> results) {
        List<String> actions = new ArrayList<>();
        
        for (DataQualityResult result : results) {
            if (!result.isPassed()) {
                String action = String.format("Applied %s rectification", 
                    result.getMetric().getDisplayName().toLowerCase());
                actions.add(action);
            }
        }
        
        return actions;
    }
    
    private List<String> generateBasicRecommendations(List<DataQualityResult> results) {
        List<String> recommendations = new ArrayList<>();
        
        for (DataQualityResult result : results) {
            if (!result.isPassed() && result.getRecommendations() != null) {
                recommendations.addAll(result.getRecommendations());
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Review data collection processes");
            recommendations.add("Implement data validation rules");
            recommendations.add("Establish regular data quality monitoring");
        }
        
        return recommendations.stream().distinct().collect(Collectors.toList());
    }
}