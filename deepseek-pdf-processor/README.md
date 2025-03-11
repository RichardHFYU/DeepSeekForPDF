# Deepseek PDF Processor

A Spring Boot batch application that processes PDF files using the Deepseek API. This application can process multiple PDF files in parallel, extract metadata, and generate structured JSON responses using Deepseek's AI capabilities.

## Features

- Batch processing of multiple PDF files
- PDF metadata extraction (title, author, page count)
- Integration with Deepseek API for AI-powered PDF analysis
- Configurable input/output directories
- Customizable prompts for Deepseek API
- JSON output for each processed PDF
- Error handling and detailed logging
- H2 in-memory database for job tracking

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Deepseek API key
- Sufficient disk space for PDF processing
- Git (optional, for version control)

## Project Structure

```
deepseek-pdf-processor/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/deepseekpdf/
│   │   │       ├── config/
│   │   │       ├── job/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       └── DeepseekPdfProcessorApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── prompts/
│   │           └── default-prompt.txt
├── input/
│   └── pdf/
├── output/
│   └── json/
├── pom.xml
└── README.md
```

## Configuration

### 1. Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Directories
deepseek.pdf.input-directory=input/pdf
deepseek.pdf.output-directory=output/json

# Deepseek API
deepseek.api.url=https://api.deepseek.com
deepseek.api.key=your_api_key_here

# Custom prompt configuration
deepseek.api.prompt-file=classpath:prompts/default-prompt.txt
deepseek.api.prompt=${your_custom_prompt_here}

# Batch configuration
deepseek.batch.chunk-size=10
spring.batch.job.enabled=false

# Logging
logging.level.com.example.deepseekpdf=DEBUG
```

### 2. Custom Prompts

You have three ways to configure your Deepseek prompts:

1. **Direct in application.properties**:
   ```properties
   deepseek.api.prompt=Please analyze this PDF and extract the following information: 1) Main topics 2) Key findings 3) Conclusions
   ```

2. **Using a prompt file**:
   - Create `src/main/resources/prompts/default-prompt.txt`
   - Add your prompt text
   - Configure in application.properties:
     ```properties
     deepseek.api.prompt-file=classpath:prompts/default-prompt.txt
     ```

3. **Programmatically** (for dynamic prompts):
   - Modify `PdfItemProcessor.java`
   - Override the `defaultPrompt` with your custom logic

### 3. PDF File Placement

Place your PDF files in the input directory:

1. **Default location**: `input/pdf/`
2. **Custom location**: 
   - Create your preferred directory
   - Update `deepseek.pdf.input-directory` in application.properties

## Running the Application

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/deepseek-pdf-processor-0.0.1-SNAPSHOT.jar
   ```

3. **Start a batch job**:
   ```bash
   curl -X POST 'http://localhost:8080/api/batch/start'
   ```

## Output

The application generates JSON files in the output directory (`output/json/` by default) with the following structure:

```json
{
  "id": "uuid",
  "fileName": "example.pdf",
  "status": "COMPLETED",
  "metadata": {
    "pageCount": 10,
    "title": "Document Title",
    "author": "Author Name"
  },
  "deepseekResponse": {
    "analysis": "...",
    "topics": [...],
    "keyFindings": [...],
    "conclusions": [...]
  },
  "processingStartTime": "2024-03-15T10:30:00",
  "processingEndTime": "2024-03-15T10:30:05"
}
```

## Example Prompts

Here are some example prompts you can use:

1. **General Analysis**:
   ```
   Please analyze this PDF document and provide:
   1. A summary of the main content
   2. Key topics discussed
   3. Important findings or conclusions
   4. Any recommendations or action items
   Format the response as JSON with these sections clearly separated.
   ```

2. **Academic Paper Analysis**:
   ```
   This is an academic paper. Please extract:
   1. Research objectives
   2. Methodology
   3. Key findings
   4. Conclusions
   5. Future research suggestions
   Structure the response in JSON format with clear sections.
   ```

3. **Business Document Analysis**:
   ```
   Analyze this business document and provide:
   1. Executive summary
   2. Key business metrics
   3. Market analysis
   4. Recommendations
   5. Risk factors
   Return the analysis in JSON format.
   ```

## Monitoring and Logging

- Access H2 Console: `http://localhost:8080/h2-console`
- View logs: Check `logs/` directory or console output
- Batch job status: `http://localhost:8080/api/batch/status`

## Error Handling

The application handles various error scenarios:

1. **PDF Reading Errors**:
   - Invalid PDF format
   - Corrupted files
   - Access permission issues

2. **API Errors**:
   - Connection timeouts
   - Authentication failures
   - Rate limiting

3. **Processing Errors**:
   - Memory issues
   - Invalid content
   - Timeout issues

All errors are logged and can be found in the application logs.

## Best Practices

1. **PDF Files**:
   - Keep files under 10MB for optimal processing
   - Ensure PDFs are not password-protected
   - Use text-based PDFs rather than scanned documents

2. **Prompts**:
   - Be specific in your requirements
   - Structure prompts for JSON-friendly responses
   - Include format requirements in the prompt

3. **System Resources**:
   - Monitor memory usage
   - Adjust batch chunk size based on PDF sizes
   - Configure appropriate timeouts

## Troubleshooting

Common issues and solutions:

1. **Application won't start**:
   - Check Java version
   - Verify application.properties configuration
   - Ensure ports are available

2. **PDFs not processing**:
   - Check input directory permissions
   - Verify PDF file format
   - Check file size limits

3. **API errors**:
   - Verify API key
   - Check network connectivity
   - Review API rate limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 