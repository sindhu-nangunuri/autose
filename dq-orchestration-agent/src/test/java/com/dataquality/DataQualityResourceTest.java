package com.dataquality;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class DataQualityResourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
          .when().get("/api/data-quality/health")
          .then()
             .statusCode(200)
             .body("status", is("healthy"));
    }

    @Test
    public void testGetAvailableMetrics() {
        given()
          .when().get("/api/data-quality/metrics")
          .then()
             .statusCode(200)
             .body("metrics", notNullValue())
             .body("descriptions", notNullValue());
    }

    @Test
    public void testGenerateSampleDataset() {
        given()
          .when().post("/api/data-quality/sample-dataset")
          .then()
             .statusCode(200)
             .body("id", notNullValue())
             .body("name", is("Sample Employee Dataset"))
             .body("columns", notNullValue())
             .body("data", notNullValue());
    }

    @Test
    public void testAnalyzeDataset() {
        Map<String, Object> dataset = Map.of(
            "id", "test-dataset",
            "name", "Test Dataset",
            "columns", List.of("id", "name", "email"),
            "data", List.of(
                Map.of("id", 1, "name", "John Doe", "email", "john@example.com"),
                Map.of("id", 2, "name", "Jane Smith", "email", "jane@example.com")
            )
        );

        given()
          .contentType(ContentType.JSON)
          .body(dataset)
          .when().post("/api/data-quality/analyze")
          .then()
             .statusCode(200)
             .body("id", notNullValue())
             .body("datasetName", is("Test Dataset"))
             .body("preProcessingScore", notNullValue())
             .body("postProcessingScore", notNullValue())
             .body("results", notNullValue());
    }

    @Test
    public void testCalculateScore() {
        Map<String, Object> dataset = Map.of(
            "id", "test-dataset",
            "name", "Test Dataset",
            "columns", List.of("id", "name"),
            "data", List.of(
                Map.of("id", 1, "name", "John Doe"),
                Map.of("id", 2, "name", "Jane Smith")
            )
        );

        given()
          .contentType(ContentType.JSON)
          .body(dataset)
          .when().post("/api/data-quality/score")
          .then()
             .statusCode(200)
             .body("overallScore", notNullValue())
             .body("grade", notNullValue())
             .body("metricScores", notNullValue());
    }

    @Test
    public void testAnalyzeOnly() {
        Map<String, Object> dataset = Map.of(
            "id", "test-dataset",
            "name", "Test Dataset",
            "columns", List.of("id", "name"),
            "data", List.of(
                Map.of("id", 1, "name", "John Doe"),
                Map.of("id", 2, "name", "Jane Smith")
            )
        );

        given()
          .contentType(ContentType.JSON)
          .body(dataset)
          .when().post("/api/data-quality/analyze-only")
          .then()
             .statusCode(200)
             .body("results", notNullValue())
             .body("score", notNullValue());
    }

    @Test
    public void testAnalyzeEmptyDataset() {
        Map<String, Object> dataset = Map.of(
            "id", "empty-dataset",
            "name", "Empty Dataset",
            "columns", List.of("id", "name"),
            "data", List.of()
        );

        given()
          .contentType(ContentType.JSON)
          .body(dataset)
          .when().post("/api/data-quality/analyze")
          .then()
             .statusCode(400)
             .body("error", is("Dataset must contain data"));
    }
}