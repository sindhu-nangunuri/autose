package com.dataquality.services;

import com.dataquality.models.Dataset;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.GraphServiceClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class SharePointService {
    
    private static final Logger logger = Logger.getLogger(SharePointService.class);
    
    @ConfigProperty(name = "sharepoint.tenant-id")
    String tenantId;
    
    @ConfigProperty(name = "sharepoint.client-id")
    String clientId;
    
    @ConfigProperty(name = "sharepoint.client-secret")
    String clientSecret;
    
    @ConfigProperty(name = "sharepoint.site-url")
    String siteUrl;
    
    @ConfigProperty(name = "sharepoint.document-library", defaultValue = "Documents")
    String documentLibrary;
    
    @ConfigProperty(name = "sharepoint.file.supported-formats", defaultValue = "xlsx,xls,csv,json")
    String supportedFormats;
    
    @ConfigProperty(name = "sharepoint.file.max-size-mb", defaultValue = "50")
    int maxSizeMb;
    
    @Inject
    ObjectMapper objectMapper;
    
    private GraphServiceClient<okhttp3.Request> graphServiceClient;
    
    public Dataset fetchFileFromSharePoint(String fileName) {
        try {
            logger.info("Fetching file from SharePoint: " + fileName);
            
            // Initialize Graph client if not already done
            if (graphServiceClient == null) {
                initializeGraphClient();
            }
            
            // Get the file from SharePoint
            DriveItem file = findFileInSharePoint(fileName);
            if (file == null) {
                throw new RuntimeException("File not found in SharePoint: " + fileName);
            }
            
            // Download file content
            byte[] fileContent = downloadFileContent(file);
            
            // Convert to Dataset based on file type
            String fileExtension = getFileExtension(fileName);
            Dataset dataset = convertToDataset(fileName, fileContent, fileExtension);
            
            logger.info("Successfully converted SharePoint file to dataset: " + fileName);
            return dataset;
            
        } catch (Exception e) {
            logger.error("Error fetching file from SharePoint: " + fileName, e);
            throw new RuntimeException("Failed to fetch file from SharePoint: " + e.getMessage(), e);
        }
    }
    
    public List<String> listAvailableFiles() {
        try {
            logger.info("Listing available files in SharePoint");
            
            if (graphServiceClient == null) {
                initializeGraphClient();
            }
            
            // This is a simplified implementation
            // In a real scenario, you would list files from the document library
            List<String> files = new ArrayList<>();
            
            // For now, return a mock list - you can implement actual SharePoint listing
            files.add("sample-data.xlsx");
            files.add("employee-data.csv");
            files.add("sales-data.json");
            
            return files;
            
        } catch (Exception e) {
            logger.error("Error listing SharePoint files", e);
            throw new RuntimeException("Failed to list SharePoint files: " + e.getMessage(), e);
        }
    }
    
    private void initializeGraphClient() {
        try {
            logger.info("Initializing Microsoft Graph client");
            
            // For now, we'll create a mock implementation
            // In a real scenario, you would use proper authentication
            logger.warn("Using mock SharePoint implementation - configure proper credentials for production");
            
            // Mock initialization - replace with actual Graph SDK initialization
            // IAuthenticationProvider authProvider = new TokenCredentialAuthProvider(
            //     Arrays.asList("https://graph.microsoft.com/.default"),
            //     new ClientSecretCredential(tenantId, clientId, clientSecret)
            // );
            // graphServiceClient = GraphServiceClient.builder()
            //     .authenticationProvider(authProvider)
            //     .buildClient();
            
        } catch (Exception e) {
            logger.error("Failed to initialize Graph client", e);
            throw new RuntimeException("Graph client initialization failed", e);
        }
    }
    
    private DriveItem findFileInSharePoint(String fileName) {
        // Mock implementation - replace with actual SharePoint search
        logger.info("Searching for file in SharePoint: " + fileName);
        
        // For demo purposes, we'll simulate finding the file
        // In reality, you would search the SharePoint document library
        return null; // This would be the actual DriveItem from SharePoint
    }
    
    private byte[] downloadFileContent(DriveItem file) {
        // Mock implementation - replace with actual file download
        logger.info("Downloading file content from SharePoint");
        
        // For demo purposes, return sample Excel data
        return createSampleExcelData();
    }
    
    private Dataset convertToDataset(String fileName, byte[] fileContent, String fileExtension) {
        try {
            switch (fileExtension.toLowerCase()) {
                case "xlsx":
                case "xls":
                    return convertExcelToDataset(fileName, fileContent);
                case "csv":
                    return convertCsvToDataset(fileName, fileContent);
                case "json":
                    return convertJsonToDataset(fileName, fileContent);
                default:
                    throw new UnsupportedOperationException("Unsupported file format: " + fileExtension);
            }
        } catch (Exception e) {
            logger.error("Error converting file to dataset: " + fileName, e);
            throw new RuntimeException("File conversion failed: " + e.getMessage(), e);
        }
    }
    
    private Dataset convertExcelToDataset(String fileName, byte[] fileContent) throws IOException {
        logger.info("Converting Excel file to dataset: " + fileName);
        
        try (InputStream inputStream = new ByteArrayInputStream(fileContent);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0); // Use first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            
            List<String> columns = new ArrayList<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            // Read header row
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    columns.add(getCellValueAsString(cell));
                }
            }
            
            // Read data rows
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = new HashMap<>();
                
                for (int i = 0; i < columns.size() && i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    String columnName = columns.get(i);
                    Object cellValue = getCellValue(cell);
                    rowData.put(columnName, cellValue);
                }
                
                data.add(rowData);
            }
            
            Dataset dataset = new Dataset(UUID.randomUUID().toString(), fileName, columns, data);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "SharePoint");
            metadata.put("originalFileName", fileName);
            metadata.put("fileType", "Excel");
            metadata.put("importedAt", LocalDateTime.now().toString());
            dataset.setMetadata(metadata);
            
            return dataset;
        }
    }
    
    private Dataset convertCsvToDataset(String fileName, byte[] fileContent) {
        logger.info("Converting CSV file to dataset: " + fileName);
        
        // Simple CSV parsing - in production, use a proper CSV library
        String content = new String(fileContent);
        String[] lines = content.split("\n");
        
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        if (lines.length > 0) {
            // Parse header
            String[] headers = lines[0].split(",");
            for (String header : headers) {
                columns.add(header.trim().replace("\"", ""));
            }
            
            // Parse data rows
            for (int i = 1; i < lines.length; i++) {
                String[] values = lines[i].split(",");
                Map<String, Object> rowData = new HashMap<>();
                
                for (int j = 0; j < columns.size() && j < values.length; j++) {
                    String value = values[j].trim().replace("\"", "");
                    rowData.put(columns.get(j), parseValue(value));
                }
                
                data.add(rowData);
            }
        }
        
        Dataset dataset = new Dataset(UUID.randomUUID().toString(), fileName, columns, data);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "SharePoint");
        metadata.put("originalFileName", fileName);
        metadata.put("fileType", "CSV");
        metadata.put("importedAt", LocalDateTime.now().toString());
        dataset.setMetadata(metadata);
        
        return dataset;
    }
    
    private Dataset convertJsonToDataset(String fileName, byte[] fileContent) throws IOException {
        logger.info("Converting JSON file to dataset: " + fileName);
        
        String content = new String(fileContent);
        JsonNode jsonNode = objectMapper.readTree(content);
        
        // Assume JSON is an array of objects
        if (jsonNode.isArray() && jsonNode.size() > 0) {
            JsonNode firstObject = jsonNode.get(0);
            List<String> columns = new ArrayList<>();
            firstObject.fieldNames().forEachRemaining(columns::add);
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                Map<String, Object> rowData = objectMapper.convertValue(node, Map.class);
                data.add(rowData);
            }
            
            Dataset dataset = new Dataset(UUID.randomUUID().toString(), fileName, columns, data);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "SharePoint");
            metadata.put("originalFileName", fileName);
            metadata.put("fileType", "JSON");
            metadata.put("importedAt", LocalDateTime.now().toString());
            dataset.setMetadata(metadata);
            
            return dataset;
        } else {
            throw new IllegalArgumentException("JSON file must contain an array of objects");
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private Object parseValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Try to parse as number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Try to parse as boolean
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            }
            // Return as string
            return value;
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    private byte[] createSampleExcelData() {
        // Create a sample Excel file for demonstration
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sample Data");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"id", "name", "email", "age", "salary", "department"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            Object[][] data = {
                {1, "John Doe", "john.doe@example.com", 30, 50000, "Engineering"},
                {2, "Jane Smith", "jane.smith@example.com", 25, 45000, "Marketing"},
                {3, "", "invalid-email", -5, 60000, "Engineering"},
                {4, "Bob Johnson", "bob.johnson@example.com", 35, null, "Sales"},
                {5, "Alice Brown", "alice.brown@example.com", 28, 1000000, "HR"}
            };
            
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    Object value = data[i][j];
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else if (value instanceof Double) {
                        cell.setCellValue((Double) value);
                    } else if (value == null) {
                        cell.setCellValue("");
                    }
                }
            }
            
            // Convert to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("Error creating sample Excel data", e);
            return new byte[0];
        }
    }
}