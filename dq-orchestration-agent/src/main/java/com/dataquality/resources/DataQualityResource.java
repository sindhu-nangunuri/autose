package com.dataquality.resources;

import com.dataquality.models.*;
import com.dataquality.orchestration.DataQualityOrchestrator;
import com.dataquality.services.GeminiService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/data-quality")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Data Quality", description = "Data Quality Analysis and Rectification APIs")
public class DataQualityResource {
    
    private static final Logger logger = Logger.getLogger(DataQualityResource.class);
    
    @Inject
    DataQualityOrchestrator orchestrator;
    
    @Inject
    GeminiService geminiService;
    
    @POST
    @Path("/analyze")
    @Operation(summary = "Analyze dataset for data quality issues")
    @APIResponse(responseCode = "200", description = "Analysis completed successfully",
                content = @Content(schema = @Schema(implementation = DataQualityReport.class)))
    @APIResponse(responseCode = "400", description = "Invalid dataset provided")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response analyzeDataset(Dataset dataset) {
        try {
            logger.info("Received data quality analysis request for dataset: " + (dataset != null ? dataset.getName() : "null"));
            
            if (dataset == null || dataset.getData() == null || dataset.getData().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Dataset must contain data"))
                    .build();
            }
            
            DataQualityReport report = orchestrator.processDataset(dataset);
            
            return Response.ok(report).build();
            
        } catch (Exception e) {
            logger.error("Error analyzing dataset", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Analysis failed: " + e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/score")
    @Operation(summary = "Calculate data quality score for dataset")
    @APIResponse(responseCode = "200", description = "Score calculated successfully",
                content = @Content(schema = @Schema(implementation = DataQualityScore.class)))
    public Response calculateScore(Dataset dataset) {
        try {
            logger.info("Calculating data quality score for dataset: " + (dataset != null ? dataset.getName() : "null"));
            
            if (dataset == null || dataset.getData() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Dataset is required"))
                    .build();
            }
            
            DataQualityScore score = orchestrator.calculateDataQualityScore(dataset);
            
            return Response.ok(score).build();
            
        } catch (Exception e) {
            logger.error("Error calculating score", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Score calculation failed: " + e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/analyze-only")
    @Operation(summary = "Analyze dataset without rectification")
    @APIResponse(responseCode = "200", description = "Analysis completed successfully")
    public Response analyzeOnly(Dataset dataset) {
        try {
            logger.info("Analyzing dataset without rectification: " + (dataset != null ? dataset.getName() : "null"));
            
            if (dataset == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Dataset is required"))
                    .build();
            }
            
            List<DataQualityResult> results = orchestrator.analyzeDataQuality(dataset);
            
            return Response.ok(Map.of(
                "results", results,
                "score", orchestrator.calculateDataQualityScore(dataset)
            )).build();
            
        } catch (Exception e) {
            logger.error("Error in analysis-only mode", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Analysis failed: " + e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/recommendations")
    @Operation(summary = "Get AI-powered recommendations for data quality improvement")
    @APIResponse(responseCode = "200", description = "Recommendations generated successfully")
    public Response getRecommendations(List<DataQualityResult> results) {
        try {
            logger.info("Generating recommendations for " + results.size() + " results");
            
            List<String> recommendations = orchestrator.generateRecommendations(results);
            
            return Response.ok(Map.of("recommendations", recommendations)).build();
            
        } catch (Exception e) {
            logger.error("Error generating recommendations", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Recommendation generation failed: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/metrics")
    @Operation(summary = "Get available data quality metrics")
    @APIResponse(responseCode = "200", description = "Metrics retrieved successfully")
    public Response getAvailableMetrics() {
        try {
            Map<String, String> descriptions = new HashMap<>();
            descriptions.put("COMPLETENESS", "Measures the percentage of non-null values");
            descriptions.put("UNIQUENESS", "Measures the percentage of unique values");
            descriptions.put("CONSISTENCY", "Measures data consistency across related fields");
            descriptions.put("VALIDITY", "Measures adherence to defined formats and rules");
            descriptions.put("ACCURACY", "Measures correctness of data values");
            descriptions.put("INTEGRITY", "Measures referential and domain integrity");
            descriptions.put("TIMELINESS", "Measures data freshness and currency");
            descriptions.put("CONFORMITY", "Measures adherence to data standards");
            descriptions.put("RANGE", "Measures values within expected ranges");
            descriptions.put("BLANKS", "Measures presence of blank/empty values");
            descriptions.put("OUTLIERS", "Measures presence of statistical outliers");
            
            Map<String, Object> metrics = Map.of(
                "metrics", DataQualityMetric.values(),
                "descriptions", descriptions
            );
            
            return Response.ok(metrics).build();
            
        } catch (Exception e) {
            logger.error("Error retrieving metrics", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve metrics"))
                .build();
        }
    }
    
    @GET
    @Path("/health")
    @Operation(summary = "Health check endpoint")
    @APIResponse(responseCode = "200", description = "Service is healthy")
    public Response healthCheck() {
        return Response.ok(Map.of(
            "status", "healthy",
            "service", "Data Quality Orchestration Agent",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis()
        )).build();
    }
    
    @POST
    @Path("/sample-dataset")
    @Operation(summary = "Generate a sample dataset for testing")
    @APIResponse(responseCode = "200", description = "Sample dataset generated successfully")
    public Response generateSampleDataset() {
        try {
            Dataset sampleDataset = createSampleDataset();
            return Response.ok(sampleDataset).build();
        } catch (Exception e) {
            logger.error("Error generating sample dataset", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to generate sample dataset"))
                .build();
        }
    }
    
    @POST
    @Path("/prompt")
    @Operation(summary = "Process user prompt using AI assistant")
    @APIResponse(responseCode = "200", description = "Prompt processed successfully")
    @APIResponse(responseCode = "400", description = "Invalid prompt provided")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response processPrompt(Map<String, String> request) {
        try {
            String prompt = request.get("prompt");
            
            if (prompt == null || prompt.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Prompt is required and cannot be empty"))
                    .build();
            }
            
            logger.info("Processing user prompt: " + prompt.substring(0, Math.min(prompt.length(), 100)) + "...");
            
            String response = geminiService.processUserPrompt(prompt.trim());
            
            return Response.ok(Map.of(
                "response", response,
                "timestamp", System.currentTimeMillis(),
                "status", "success"
            )).build();
            
        } catch (Exception e) {
            logger.error("Error processing user prompt", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to process prompt: " + e.getMessage()))
                .build();
        }
    }
    
    private Dataset createSampleDataset() {
        List<String> columns = List.of("id", "name", "email", "age", "salary", "department");
        
        // Using HashMap instead of Map.of() to allow null values
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        
        Map<String, Object> row1 = new java.util.HashMap<>();
        row1.put("id", 1);
        row1.put("name", "John Doe");
        row1.put("email", "john.doe@example.com");
        row1.put("age", 30);
        row1.put("salary", 50000);
        row1.put("department", "Engineering");
        data.add(row1);
        
        Map<String, Object> row2 = new java.util.HashMap<>();
        row2.put("id", 2);
        row2.put("name", "Jane Smith");
        row2.put("email", "jane.smith@example.com");
        row2.put("age", 25);
        row2.put("salary", 45000);
        row2.put("department", "Marketing");
        data.add(row2);
        
        Map<String, Object> row3 = new java.util.HashMap<>();
        row3.put("id", 3);
        row3.put("name", "");
        row3.put("email", "invalid-email");
        row3.put("age", -5);
        row3.put("salary", 60000);
        row3.put("department", "Engineering");
        data.add(row3);
        
        Map<String, Object> row4 = new java.util.HashMap<>();
        row4.put("id", 4);
        row4.put("name", "Bob Johnson");
        row4.put("email", "bob.johnson@example.com");
        row4.put("age", 35);
        row4.put("salary", null);
        row4.put("department", "Sales");
        data.add(row4);
        
        Map<String, Object> row5 = new java.util.HashMap<>();
        row5.put("id", 1);
        row5.put("name", "John Doe");
        row5.put("email", "john.doe@example.com");
        row5.put("age", 30);
        row5.put("salary", 50000);
        row5.put("department", "Engineering");
        data.add(row5); // Duplicate
        
        Map<String, Object> row6 = new java.util.HashMap<>();
        row6.put("id", 5);
        row6.put("name", "Alice Brown");
        row6.put("email", "alice.brown@example.com");
        row6.put("age", 28);
        row6.put("salary", 1000000);
        row6.put("department", "HR");
        data.add(row6); // Outlier
        
        Dataset dataset = new Dataset(UUID.randomUUID().toString(), "Sample Employee Dataset", columns, data);
        
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("description", "Sample dataset with various data quality issues for testing");
        metadata.put("source", "Generated for demonstration purposes");
        dataset.setMetadata(metadata);
        
        return dataset;
    }
}