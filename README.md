# Deepseek PDF Processor

A Spring Boot application that processes PDF documents using the Deepseek API, with robust error handling and comprehensive logging.

## Features

- PDF document processing with Deepseek API integration
- Streaming and non-streaming processing modes
- Comprehensive error handling with custom exceptions
- Detailed logging with operation tracking
- Configurable prompts and model parameters
- File-based and direct prompt configuration
- Batch processing support

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Deepseek API key

## Configuration

### Application Properties

```properties
# Deepseek SDK configuration
deepseek.api-key=your_api_key_here
deepseek.model=deepseek-coder
deepseek.timeout=60000
deepseek.max-retries=3

# Model parameters
deepseek.temperature=0.7
deepseek.max-tokens=4096

# Custom prompt configuration
deepseek.api.prompt-file=classpath:prompts/default-prompt.txt
# deepseek.api.prompt=Your direct prompt here
```

### Directory Structure

```
input/pdf/    - Place PDF files here for processing
output/json/  - Processed results will be saved here
logs/         - Application logs directory
```

## Error Handling

The application implements a robust error handling system using custom exceptions:

### Error Codes

| Code     | Description                        | Common Causes                          |
|----------|------------------------------------|---------------------------------------|
| DEEP_001 | Prompt file/config not found       | Missing prompt file or configuration  |
| DEEP_002 | PDF processing error               | Invalid PDF or processing failure     |
| DEEP_003 | API communication error            | Network issues or API unavailability  |
| DEEP_004 | Invalid API response               | Unexpected API response format        |
| DEEP_005 | Stream processing error            | Issues during streaming operation     |
| DEEP_006 | Configuration error                | Invalid application configuration     |

### Exception Handling Example

```java
try {
    DeepseekApiResponse response = deepseekApiService.processPdf(pdfDocument);
    // Process response
} catch (DeepseekApiException e) {
    switch (e.getErrorCode()) {
        case PDF_PROCESSING_ERROR:
            log.error("PDF processing failed: {}", e.getDetails());
            // Handle PDF processing error
            break;
        case API_COMMUNICATION_ERROR:
            log.error("API communication error: {}", e.getDetails());
            // Handle API error
            break;
    }
}
```

## Logging System

### Log Levels

- TRACE: Detailed debugging information
- DEBUG: Debugging information, API requests/responses
- INFO: General application events
- WARN: Warning messages
- ERROR: Error messages and stack traces

### Log Format

```
2024-03-14 10:15:30 [thread-1] [OP1234] INFO  com.example.deepseekpdf.service.DeepseekApiService - Message
```

Components:
- Timestamp: `2024-03-14 10:15:30`
- Thread: `[thread-1]`
- Operation ID: `[OP1234]`
- Log Level: `INFO`
- Logger: `com.example.deepseekpdf.service.DeepseekApiService`
- Message: The actual log message

### Log Configuration

```properties
# Log levels
logging.level.root=INFO
logging.level.com.example.deepseekpdf=DEBUG

# Log file settings
logging.file.name=logs/deepseek-pdf-processor.log
logging.file.max-size=10MB
logging.file.max-history=10
```

## Usage

### Basic Processing

```java
@Autowired
private DeepseekApiService deepseekApiService;

public void processPdf(PdfDocument document) {
    DeepseekApiResponse response = deepseekApiService.processPdf(document);
    // Handle response
}
```

### Streaming Processing

```java
public void processPdfWithStreaming(PdfDocument document) {
    deepseekApiService.processPdfStream(document)
        .subscribe(
            response -> System.out.println("Chunk received: " + response.getRawResponse()),
            error -> System.err.println("Error: " + error.getMessage()),
            () -> System.out.println("Processing completed")
        );
}
```

### Custom Prompt

```java
String customPrompt = "Please analyze this technical document...";
DeepseekApiResponse response = deepseekApiService.processPdf(document, customPrompt);
```

## Monitoring and Troubleshooting

### Log Files

- Main log file: `logs/deepseek-pdf-processor.log`
- Log rotation: 10 files, 10MB each
- Console output: Colored for better readability

### Common Issues

1. **Configuration Errors**
   - Check `application.properties` for correct values
   - Verify API key is properly set
   - Ensure prompt file exists if using file-based prompts

2. **PDF Processing Errors**
   - Verify PDF file is valid and readable
   - Check file permissions
   - Ensure PDF content is within size limits

3. **API Communication Issues**
   - Verify network connectivity
   - Check API key validity
   - Confirm API endpoint is accessible

## Development

### Adding New Error Codes

1. Add new error code to `DeepseekApiException.ErrorCode`:
```java
public enum ErrorCode {
    // Existing codes...
    NEW_ERROR("DEEP_XXX", "Description");
}
```

2. Implement error handling in service:
```java
throw new DeepseekApiException(
    ErrorCode.NEW_ERROR,
    "Error message",
    "Additional details"
);
```

### Customizing Logging

1. Modify log patterns in `application.properties`
2. Add new log statements using SLF4J:
```java
log.debug("Debug message: {}", value);
log.info("Info message: {}", value);
log.error("Error message: {}", value, exception);
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 