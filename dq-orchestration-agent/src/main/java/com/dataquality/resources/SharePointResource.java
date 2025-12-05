package com.dataquality.resources;

import com.dataquality.models.Dataset;
import com.dataquality.models.DataQualityReport;
import com.dataquality.orchestration.DataQualityOrchestrator;
import com.dataquality.services.SharePointService;
import jakarta.inject.Inject;
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

@Path("/api/sharepoint")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SharePoint Integration", description = "SharePoint file processing and data quality analysis")
public class SharePointResource {
    
    private static final Logger logger = Logger.getLogger(SharePointResource.class);
    
    @Inject
    SharePointService sharePointService;
    
    @Inject
    DataQualityOrchestrator orchestrator;
    
    @GET
    @Path("/files")
    @Operation(summary = "List available files in SharePoint")
    @APIResponse(responseCode = "200", description = "Files listed successfully")
    @APIResponse(responseCode = "500", description = "Error accessing SharePoint")
    public Response listFiles() {
        try {
            logger.info("Listing available files in SharePoint");
            
            List<String> files = sharePointService.listAvailableFiles();
            
            return Response.ok(Map.of(
                "files", files,
                "count", files.size(),
                "timestamp", System.currentTimeMillis()
            )).build();
            
        } catch (Exception e) {
            logger.error("Error listing SharePoint files", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to list SharePoint files: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/files/{fileName}")
    @Operation(summary = "Fetch and convert a file from SharePoint to Dataset")
    @APIResponse(responseCode = "200", description = "File converted successfully",
                content = @Content(schema = @Schema(implementation = Dataset.class)))
    @APIResponse(responseCode = "404", description = "File not found")
    @APIResponse(responseCode = "400", description = "Unsupported file format")
    @APIResponse(responseCode = "500", description = "Error processing file")
    public Response fetchFile(@PathParam("fileName") String fileName) {
        try {
            logger.info("Fetching file from SharePoint: " + fileName);
            
            Dataset dataset = sharePointService.fetchFileFromSharePoint(fileName);
            
            return Response.ok(dataset).build();
            
        } catch (RuntimeException e) {
            logger.error("Error fetching file from SharePoint: " + fileName, e);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "File not found: " + fileName))
                    .build();
            } else if (errorMessage != null && errorMessage.contains("Unsupported")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", errorMessage))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to process file: " + (errorMessage != null ? errorMessage : e.getClass().getSimpleName())))
                    .build();
            }
        }
    }
    
    @POST
    @Path("/analyze/{fileName}")
    @Operation(summary = "Fetch file from SharePoint and perform data quality analysis")
    @APIResponse(responseCode = "200", description = "Analysis completed successfully",
                content = @Content(schema = @Schema(implementation = DataQualityReport.class)))
    @APIResponse(responseCode = "404", description = "File not found")
    @APIResponse(responseCode = "400", description = "Invalid file or unsupported format")
    @APIResponse(responseCode = "500", description = "Analysis failed")
    public Response analyzeSharePointFile(@PathParam("fileName") String fileName) {
        try {
            logger.info("Analyzing SharePoint file: " + fileName);
            
            // Step 1: Fetch file from SharePoint and convert to Dataset
            Dataset dataset = sharePointService.fetchFileFromSharePoint(fileName);
            
            // Step 2: Perform data quality analysis
            DataQualityReport report = orchestrator.processDataset(dataset);
            
            // Add SharePoint-specific metadata to the report
            Map<String, Object> metadata = report.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
                report.setMetadata(metadata);
            } else {
                // Create a mutable copy if the existing metadata is immutable
                metadata = new HashMap<>(metadata);
                report.setMetadata(metadata);
            }
            metadata.put("sourceType", "SharePoint");
            metadata.put("originalFileName", fileName);
            
            logger.info("Successfully analyzed SharePoint file: " + fileName);
            
            return Response.ok(report).build();
            
        } catch (RuntimeException e) {
            logger.error("Error analyzing SharePoint file: " + fileName, e);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "File not found: " + fileName))
                    .build();
            } else if (errorMessage != null && errorMessage.contains("Unsupported")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", errorMessage))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Analysis failed: " + (errorMessage != null ? errorMessage : e.getClass().getSimpleName())))
                    .build();
            }
        }
    }
    
    @POST
    @Path("/analyze-only/{fileName}")
    @Operation(summary = "Fetch file from SharePoint and analyze without rectification")
    @APIResponse(responseCode = "200", description = "Analysis completed successfully")
    @APIResponse(responseCode = "404", description = "File not found")
    @APIResponse(responseCode = "500", description = "Analysis failed")
    public Response analyzeSharePointFileOnly(@PathParam("fileName") String fileName) {
        try {
            logger.info("Analyzing SharePoint file (analysis-only): " + fileName);
            
            // Step 1: Fetch file from SharePoint and convert to Dataset
            Dataset dataset = sharePointService.fetchFileFromSharePoint(fileName);
            
            // Step 2: Perform analysis without rectification
            var results = orchestrator.analyzeDataQuality(dataset);
            var score = orchestrator.calculateDataQualityScore(dataset);
            
            return Response.ok(Map.of(
                "results", results,
                "score", score,
                "dataset", dataset,
                "sourceType", "SharePoint",
                "originalFileName", fileName
            )).build();
            
        } catch (RuntimeException e) {
            logger.error("Error analyzing SharePoint file: " + fileName, e);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "File not found: " + fileName))
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Analysis failed: " + (errorMessage != null ? errorMessage : e.getClass().getSimpleName())))
                    .build();
            }
        }
    }
    
    @GET
    @Path("/config")
    @Operation(summary = "Get SharePoint configuration information")
    @APIResponse(responseCode = "200", description = "Configuration retrieved successfully")
    public Response getConfiguration() {
        try {
            return Response.ok(Map.of(
                "supportedFormats", List.of("xlsx", "xls", "csv", "json"),
                "maxFileSizeMB", 50,
                "documentLibrary", "Documents",
                "status", "configured"
            )).build();
            
        } catch (Exception e) {
            logger.error("Error retrieving SharePoint configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve configuration"))
                .build();
        }
    }
    
    @GET
    @Path("/health")
    @Operation(summary = "Check SharePoint service health")
    @APIResponse(responseCode = "200", description = "Service is healthy")
    @APIResponse(responseCode = "503", description = "Service is unavailable")
    public Response healthCheck() {
        try {
            // Perform a simple health check
            // In a real implementation, you might test the SharePoint connection
            
            return Response.ok(Map.of(
                "status", "healthy",
                "service", "SharePoint Integration Service",
                "timestamp", System.currentTimeMillis(),
                "version", "1.0.0"
            )).build();
            
        } catch (Exception e) {
            logger.error("SharePoint health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of(
                    "status", "unhealthy",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
        }
    }
}