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
    public Response analyzeDataset(@Valid Dataset dataset) {
        try {
            logger.info("Received data quality analysis request for dataset: " + dataset.getName());
            
            if (dataset.getData() == null || dataset.getData().isEmpty()) {
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
    public Response calculateScore(@Valid Dataset dataset) {
        try {
            logger.info("Calculating data quality score for dataset: " + dataset.getName());
            
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
    public Response analyzeOnly(@Valid Dataset dataset) {
        try {
            logger.info("Analyzing dataset without rectification: " + dataset.getName());
            
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
            Map<String, Object> metrics = Map.of(
                "metrics", DataQualityMetric.values(),
                "descriptions", Map.of(
                    "COMPLETENESS", "Measures the percentage of non-null values",
                    "UNIQUENESS", "Measures the percentage of unique values",
                    "CONSISTENCY", "Measures data consistency across related fields",
                    "VALIDITY", "Measures adherence to defined formats and rules",
                    "ACCURACY", "Measures correctness of data values",
                    "INTEGRITY", "Measures referential and domain integrity",
                    "TIMELINESS", "Measures data freshness and currency",
                    "CONFORMITY", "Measures adherence to data standards",
                    "RANGE", "Measures values within expected ranges",
                    "BLANKS", "Measures presence of blank/empty values",
                    "OUTLIERS", "Measures presence of statistical outliers"
                )
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
    
    private Dataset createSampleDataset() {
        List<String> columns = List.of("id", "name", "email", "age", "salary", "department");
        
        List<Map<String, Object>> data = List.of(
            Map.of("id", 1, "name", "John Doe", "email", "john.doe@example.com", "age", 30, "salary", 50000, "department", "Engineering"),
            Map.of("id", 2, "name", "Jane Smith", "email", "jane.smith@example.com", "age", 25, "salary", 45000, "department", "Marketing"),
            Map.of("id", 3, "name", "", "email", "invalid-email", "age", -5, "salary", 60000, "department", "Engineering"),
            Map.of("id", 4, "name", "Bob Johnson", "email", "bob.johnson@example.com", "age", 35, "salary", null, "department", "Sales"),
            Map.of("id", 1, "name", "John Doe", "email", "john.doe@example.com", "age", 30, "salary", 50000, "department", "Engineering"), // Duplicate
            Map.of("id", 5, "name", "Alice Brown", "email", "alice.brown@example.com", "age", 28, "salary", 1000000, "department", "HR") // Outlier
        );
        
        Dataset dataset = new Dataset(UUID.randomUUID().toString(), "Sample Employee Dataset", columns, data);
        dataset.setMetadata(Map.of(
            "description", "Sample dataset with various data quality issues for testing",
            "source", "Generated for demonstration purposes"
        ));
        
        return dataset;
    }
}