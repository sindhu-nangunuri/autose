package com.dataquality.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class DataQualityResourceIntegrationTest {

    @Test
    void testGetAvailableMetrics() {
        given()
            .when().get("/api/data-quality/metrics")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("metrics", notNullValue())
            .body("descriptions", notNullValue());
    }

    @Test
    void testAnalyzeDataQuality() {
        String requestBody = """
            {
                "name": "Test Dataset",
                "columns": ["id", "name", "email"],
                "data": [
                    {"id": "1", "name": "John", "email": "john@example.com"},
                    {"id": "2", "name": "Jane", "email": "jane@example.com"},
                    {"id": "3", "name": "Bob", "email": "bob@example.com"}
                ]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("datasetName", notNullValue())
            .body("results", notNullValue())
            .body("preProcessingScore", notNullValue())
            .body("summary", notNullValue())
            .body("rectificationActions", notNullValue());
    }

    @Test
    void testAnalyzeDataQualityWithIncompleteData() {
        String requestBody = """
            {
                "name": "Incomplete Dataset",
                "columns": ["id", "name", "email"],
                "data": [
                    {"id": "1", "name": "John", "email": "john@example.com"},
                    {"id": "2", "name": "", "email": "jane@example.com"},
                    {"id": "3", "name": "Bob", "email": ""}
                ]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("datasetName", is("Incomplete Dataset"))
            .body("results", notNullValue())
            .body("preProcessingScore", notNullValue())
            .body("summary", notNullValue())
            .body("rectificationActions", notNullValue());
    }

    @Test
    void testAnalyzeDataQualityWithEmptyDataset() {
        String requestBody = """
            {
                "name": "Empty Dataset",
                "columns": [],
                "data": []
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("datasetName", notNullValue())
            .body("results", notNullValue());
    }

    @Test
    void testAnalyzeDataQualityWithInvalidRequest() {
        String invalidRequestBody = """
            {
                "invalid": "request"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidRequestBody)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(400);
    }

    @Test
    void testAnalyzeDataQualityWithNullRequest() {
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(400);
    }

    @Test
    void testHealthCheck() {
        given()
            .when().get("/api/data-quality/health")
            .then()
            .statusCode(200)
            .body("status", is("UP"));
    }

    @Test
    void testCorsHeaders() {
        given()
            .header("Origin", "http://localhost:3000")
            .when().options("/api/data-quality/metrics")
            .then()
            .statusCode(200);
    }

    @Test
    void testContentTypeValidation() {
        String requestBody = """
            {
                "name": "Test Dataset",
                "columns": ["id", "name"],
                "data": [{"id": "1", "name": "John"}]
            }
            """;

        // Test with wrong content type
        given()
            .contentType(ContentType.TEXT)
            .body(requestBody)
            .when().post("/api/data-quality/analyze")
            .then()
            .statusCode(415); // Unsupported Media Type
    }
}