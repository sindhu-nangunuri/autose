package com.dataquality.agents;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityReport;
import com.dataquality.models.DataQualityResult;
import com.dataquality.models.DataQualityScore;
import com.dataquality.services.DataQualityAnalysisService;
import com.dataquality.services.GeminiService;
import com.dataquality.services.SharePointService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class GoogleADKAgent {
    
    private static final Logger logger = Logger.getLogger(GoogleADKAgent.class);
    
    @Inject
    SharePointService sharePointService;
    
    @Inject
    DataQualityAnalysisService analysisService;
    
    @Inject
    GeminiService geminiService;
    
    @ConfigProperty(name = "google-adk.enabled", defaultValue = "true")
    boolean enabled;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Patterns to identify SharePoint file analysis requests
    private static final Pattern SHAREPOINT_FILE_PATTERN = Pattern.compile(
        "(?i).*(?:analyze|check|examine|review|assess).*(?:file|document|data).*(?:from|in|on).*(?:sharepoint|share\\s*point).*",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(
        "(?i)(?:file|document)\\s*(?:named|called)?\\s*[\"']?([\\w\\-\\.]+\\.[a-z]{2,4})[\"']?",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SPECIFIC_FILE_PATTERN = Pattern.compile(
        "(?i)[\"']([\\w\\-\\.]+\\.[a-z]{2,4})[\"']",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Process a user prompt and determine if it requires SharePoint file analysis
     */
    public Map<String, Object> processPrompt(String userPrompt) {
        logger.info("Google ADK Agent processing prompt: " + userPrompt.substring(0, Math.min(userPrompt.length(), 100)) + "...");
        
        if (!enabled) {
            logger.warn("Google ADK Agent is disabled");
            return createErrorResponse("Google ADK Agent is currently disabled");
        }
        
        try {
            // Check if the prompt is requesting SharePoint file analysis
            if (isSharePointAnalysisRequest(userPrompt)) {
                return handleSharePointAnalysisRequest(userPrompt);
            } else {
                // For non-SharePoint requests, use regular Gemini processing
                return handleGeneralPrompt(userPrompt);
            }
            
        } catch (Exception e) {
            logger.error("Error processing prompt with Google ADK Agent", e);
            return createErrorResponse("Failed to process request: " + e.getMessage());
        }
    }
    
    /**
     * Check if the prompt is requesting SharePoint file analysis
     */
    private boolean isSharePointAnalysisRequest(String prompt) {
        return SHAREPOINT_FILE_PATTERN.matcher(prompt).find() || 
               FILE_NAME_PATTERN.matcher(prompt).find() ||
               prompt.toLowerCase().contains("sharepoint") ||
               prompt.toLowerCase().contains("share point");
    }
    
    /**
     * Handle SharePoint file analysis requests
     */
    private Map<String, Object> handleSharePointAnalysisRequest(String prompt) {
        logger.info("Handling SharePoint analysis request");
        
        try {
            // Extract file name from prompt
            String fileName = extractFileName(prompt);
            
            if (fileName == null) {
                // If no specific file mentioned, list available files
                List<String> availableFiles = sharePointService.listAvailableFiles();
                return createFileListResponse(availableFiles, prompt);
            }
            
            // Fetch and analyze the specified file
            return analyzeSharePointFile(fileName, prompt);
            
        } catch (Exception e) {
            logger.error("Error handling SharePoint analysis request", e);
            return createErrorResponse("Failed to analyze SharePoint file: " + e.getMessage());
        }
    }
    
    /**
     * Extract file name from the user prompt
     */
    private String extractFileName(String prompt) {
        // Try specific file pattern first (quoted filenames)
        Matcher specificMatcher = SPECIFIC_FILE_PATTERN.matcher(prompt);
        if (specificMatcher.find()) {
            return specificMatcher.group(1);
        }
        
        // Try general file name pattern
        Matcher fileNameMatcher = FILE_NAME_PATTERN.matcher(prompt);
        if (fileNameMatcher.find()) {
            return fileNameMatcher.group(1);
        }
        
        // Check for common file extensions in the prompt
        String[] commonExtensions = {"xlsx", "xls", "csv", "json"};
        for (String ext : commonExtensions) {
            Pattern extPattern = Pattern.compile("([\\w\\-]+\\." + ext + ")", Pattern.CASE_INSENSITIVE);
            Matcher extMatcher = extPattern.matcher(prompt);
            if (extMatcher.find()) {
                return extMatcher.group(1);
            }
        }
        
        return null;
    }
    
    /**
     * Analyze a specific SharePoint file
     */
    private Map<String, Object> analyzeSharePointFile(String fileName, String originalPrompt) {
        logger.info("Analyzing SharePoint file: " + fileName);
        
        try {
            // Fetch the file from SharePoint
            Dataset dataset = sharePointService.fetchFileFromSharePoint(fileName);
            
            // Perform data quality analysis
            DataQualityReport report = analysisService.processDataset(dataset);
            
            // Generate AI summary based on the original prompt and analysis results
            String aiSummary = generateAnalysisSummary(originalPrompt, report, dataset);
            
            // Create comprehensive response
            Map<String, Object> response = new HashMap<>();
            response.put("type", "sharepoint_analysis");
            response.put("status", "success");
            response.put("fileName", fileName);
            response.put("dataset", dataset);
            response.put("report", report);
            response.put("aiSummary", aiSummary);
            response.put("timestamp", System.currentTimeMillis());
            response.put("originalPrompt", originalPrompt);
            
            // Add recommendations
            if (report.getResults() != null && !report.getResults().isEmpty()) {
                List<String> recommendations = analysisService.generateRecommendations(report.getResults());
                response.put("recommendations", recommendations);
            }
            
            logger.info("Successfully analyzed SharePoint file: " + fileName);
            return response;
            
        } catch (Exception e) {
            logger.error("Error analyzing SharePoint file: " + fileName, e);
            return createErrorResponse("Failed to analyze file '" + fileName + "': " + e.getMessage());
        }
    }
    
    /**
     * Generate AI summary of the analysis
     */
    private String generateAnalysisSummary(String originalPrompt, DataQualityReport report, Dataset dataset) {
        try {
            StringBuilder summaryPrompt = new StringBuilder();
            summaryPrompt.append("User Request: ").append(originalPrompt).append("\n\n");
            summaryPrompt.append("I have analyzed the SharePoint file '").append(dataset.getName()).append("' and here are the results:\n\n");
            
            // Add dataset information
            summaryPrompt.append("Dataset Information:\n");
            summaryPrompt.append("- File: ").append(dataset.getName()).append("\n");
            summaryPrompt.append("- Rows: ").append(dataset.getRowCount()).append("\n");
            summaryPrompt.append("- Columns: ").append(dataset.getColumnCount()).append("\n");
            if (dataset.getColumns() != null) {
                summaryPrompt.append("- Column Names: ").append(String.join(", ", dataset.getColumns())).append("\n");
            }
            summaryPrompt.append("\n");
            
            // Add quality scores
            if (report.getPreProcessingScore() != null) {
                summaryPrompt.append("Pre-processing Quality Score: ")
                    .append(String.format("%.1f%%", report.getPreProcessingScore().getOverallScore() * 100))
                    .append(" (Grade: ").append(report.getPreProcessingScore().getGrade()).append(")\n");
            }
            
            if (report.getPostProcessingScore() != null) {
                summaryPrompt.append("Post-processing Quality Score: ")
                    .append(String.format("%.1f%%", report.getPostProcessingScore().getOverallScore() * 100))
                    .append(" (Grade: ").append(report.getPostProcessingScore().getGrade()).append(")\n");
            }
            
            // Add detailed results
            if (report.getResults() != null && !report.getResults().isEmpty()) {
                summaryPrompt.append("\nDetailed Quality Metrics:\n");
                for (DataQualityResult result : report.getResults()) {
                    summaryPrompt.append("- ").append(result.getMetric().getDisplayName())
                        .append(": ").append(String.format("%.1f%%", result.getScore() * 100))
                        .append(" (").append(result.isPassed() ? "PASSED" : "FAILED").append(")\n");
                    
                    if (result.getIssues() != null && !result.getIssues().isEmpty()) {
                        summaryPrompt.append("  Issues: ").append(String.join(", ", result.getIssues())).append("\n");
                    }
                }
            }
            
            // Add rectification actions
            if (report.getRectificationActions() != null && !report.getRectificationActions().isEmpty()) {
                summaryPrompt.append("\nRectification Actions Performed:\n");
                for (String action : report.getRectificationActions()) {
                    summaryPrompt.append("- ").append(action).append("\n");
                }
            }
            
            summaryPrompt.append("\nPlease provide a comprehensive summary of these results, highlighting key findings, ");
            summaryPrompt.append("data quality issues discovered, improvements made, and actionable insights for the user. ");
            summaryPrompt.append("Format the response in a clear, professional manner suitable for business stakeholders.");
            
            return geminiService.processUserPrompt(summaryPrompt.toString());
            
        } catch (Exception e) {
            logger.error("Error generating AI summary", e);
            return "Analysis completed successfully. The file has been processed and data quality metrics have been calculated. " +
                   "Please review the detailed results for specific quality scores and recommendations.";
        }
    }
    
    /**
     * Handle general prompts that don't require SharePoint analysis
     */
    private Map<String, Object> handleGeneralPrompt(String prompt) {
        logger.info("Handling general prompt");
        
        try {
            String response = geminiService.processUserPrompt(prompt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("type", "general_response");
            result.put("status", "success");
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());
            result.put("originalPrompt", prompt);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error handling general prompt", e);
            return createErrorResponse("Failed to process general request: " + e.getMessage());
        }
    }
    
    /**
     * Create response when no specific file is mentioned
     */
    private Map<String, Object> createFileListResponse(List<String> availableFiles, String originalPrompt) {
        try {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("User Request: ").append(originalPrompt).append("\n\n");
            promptBuilder.append("The user is asking about SharePoint file analysis, but didn't specify a particular file. ");
            promptBuilder.append("Here are the available files in SharePoint:\n\n");
            
            for (int i = 0; i < availableFiles.size(); i++) {
                promptBuilder.append((i + 1)).append(". ").append(availableFiles.get(i)).append("\n");
            }
            
            promptBuilder.append("\nPlease provide a helpful response suggesting which file(s) might be relevant ");
            promptBuilder.append("for their analysis request, or ask them to specify which file they'd like to analyze. ");
            promptBuilder.append("Be friendly and professional in your response.");
            
            String aiResponse = geminiService.processUserPrompt(promptBuilder.toString());
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "file_list");
            response.put("status", "success");
            response.put("response", aiResponse);
            response.put("availableFiles", availableFiles);
            response.put("timestamp", System.currentTimeMillis());
            response.put("originalPrompt", originalPrompt);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating file list response", e);
            return createErrorResponse("Available files: " + String.join(", ", availableFiles) + 
                                     ". Please specify which file you'd like to analyze.");
        }
    }
    
    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("status", "error");
        response.put("error", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * Get available SharePoint files
     */
    public List<String> getAvailableFiles() {
        try {
            return sharePointService.listAvailableFiles();
        } catch (Exception e) {
            logger.error("Error getting available files", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if the agent is enabled and properly configured
     */
    public boolean isHealthy() {
        return enabled;
    }
}