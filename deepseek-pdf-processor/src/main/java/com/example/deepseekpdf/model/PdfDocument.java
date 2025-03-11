package com.example.deepseekpdf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a PDF document to be processed by the Deepseek API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocument {
    
    private String id;
    private String fileName;
    private String filePath;
    private byte[] content;
    private String contentType;
    private long fileSize;
    private String status;
    
    // Metadata extracted from the PDF
    private int pageCount;
    private String title;
    private String author;
    
    // Processing information
    private String processingStartTime;
    private String processingEndTime;
    private String deepseekResponse;
} 