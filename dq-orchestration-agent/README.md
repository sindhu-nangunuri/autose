# Data Quality Orchestration Agent

A multi-agent data quality analysis and rectification system built with Quarkus, Java 17, and Google Gemini LLM.

Welcome to the Data Quality Orchestration Agent! This powerful tool provides a comprehensive solution for analyzing and improving the quality of your datasets. Our multi-agent architecture evaluates data across numerous quality dimensions, offering AI-powered insights and automated rectification capabilities.

## Overview

This application provides comprehensive data quality analysis and automated rectification capabilities through a multi-agent architecture. It evaluates datasets across multiple quality dimensions and provides AI-powered insights and recommendations.

## Features

### Data Quality Metrics
- **Completeness**: Measures percentage of non-null values
- **Uniqueness**: Measures percentage of unique values  
- **Consistency**: Measures data consistency across related fields
- **Validity**: Measures adherence to defined formats and rules
- **Accuracy**: Measures correctness of data values
- **Integrity**: Measures referential and domain integrity
- **Timeliness**: Measures data freshness and currency
- **Conformity**: Measures adherence to data standards
- **Range**: Measures values within expected ranges
- **Blanks**: Measures presence of blank/empty values
- **Outliers**: Measures presence of statistical outliers

### Multi-Agent Architecture
- **Orchestration Agent**: Coordinates all worker agents and manages the overall process
- **Worker Agents**: Specialized agents for each data quality metric
- **Scoring Agent**: Calculates pre/post processing quality scores
- **Gemini Integration**: AI-powered analysis, recommendations, and insights

### Automated Rectification
- Intelligent data imputation for missing values
- Duplicate record removal
- Data format standardization
- Outlier detection and correction
- Invalid value correction

## Technology Stack

- **Framework**: Quarkus 3.6.0
- **Language**: Java 17
- **AI/ML**: Google Gemini LLM (ADK 0.3.0)
- **Build Tool**: Maven
- **Testing**: JUnit 5, REST Assured

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Google Gemini API key
### Configuration

Set your Gemini API key in `application.yml`:
```yaml
data-quality:
  gemini:
    api-key: ${GEMINI_API_KEY:your-gemini-api-key-here}
```

Or set the environment variable:
```
export GEMINI_API_KEY=your-actual-api-key
```
### Quality Thresholds
Configure quality thresholds in `application.yml`:

```yaml
data-quality:
  thresholds:
    completeness: 0.95
    uniqueness: 0.98
    validity: 0.95
    # ... other thresholds
```

### Scoring Weights
Adjust metric weights for overall scoring:

```yaml
data-quality:
  scoring:
    weights:
      completeness: 0.15
      uniqueness: 0.15
      validity: 0.15
      # ... other weights
```

### Running the Application

```
# Development mode
./mvnw compile quarkus:dev

# Production build
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

The application will start on `http://localhost:8080`

### API Documentation

Access the Swagger UI at: `http://localhost:8080/swagger-ui`

## API Endpoints

### Core Endpoints

- `POST /api/data-quality/analyze` - Full analysis and rectification
- `POST /api/data-quality/score` - Calculate quality score only
- `POST /api/data-quality/analyze-only` - Analysis without rectification
- `POST /api/data-quality/recommendations` - Get AI recommendations
- `GET /api/data-quality/metrics` - Available quality metrics
- `GET /api/data-quality/health` - Health check
- `POST /api/data-quality/sample-dataset` - Generate sample data

### Example Usage

#### Analyze Dataset
```
curl -X POST http://localhost:8080/api/data-quality/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "id": "sample-001",
    "name": "Customer Data",
    "columns": ["id", "name", "email", "age"],
    "data": [
      {"id": 1, "name": "John Doe", "email": "john@example.com", "age": 30},
      {"id": 2, "name": "", "email": "invalid-email", "age": -5}
    ]
  }'
```

#### Get Sample Dataset
```
curl -X POST http://localhost:8080/api/data-quality/sample-dataset
```

## Architecture

### Agent Pattern
Each data quality metric is handled by a specialized agent that implements:
- `analyze(Dataset)` - Evaluates the metric
- `rectify(Dataset, Result)` - Attempts to fix issues
- `getMetric()` - Returns the handled metric

### Orchestration Flow
1. **Initial Analysis**: All agents analyze the dataset in parallel
2. **Scoring**: Calculate pre-processing quality score
3. **Rectification**: Apply fixes based on analysis results
4. **Re-analysis**: Analyze rectified dataset
5. **Final Scoring**: Calculate post-processing quality score
6. **AI Insights**: Generate summary and recommendations using Gemini

## Development

### Adding New Agents
1. Implement `DataQualityAgent` interface
2. Extend `BaseDataQualityAgent` for common functionality
3. Add `@ApplicationScoped` annotation
4. Inject into `DataQualityOrchestrator`

### Testing
```
# Run tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Deployment

### Docker
```
# Build native image
./mvnw package -Pnative -Dquarkus.native.container-build=true

# Run container
docker run -i --rm -p 8080:8080 dq-orchestration-agent:1.0.0-SNAPSHOT
```

### Environment Variables
- `GEMINI_API_KEY` - Google Gemini API key (required)
- `QUARKUS_HTTP_PORT` - Server port (default: 8080)

## Monitoring

### Health Checks
- Application health: `GET /api/data-quality/health`
- Quarkus health: `GET /q/health`

### Metrics
- Quarkus metrics: `GET /q/metrics`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.