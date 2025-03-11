package com.example.deepseekpdf.service;

import com.example.deepseekpdf.model.DeepseekApiResponse;
import com.example.deepseekpdf.model.PdfDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for processing PDF files.
 */
@Service
@Slf4j
public class PdfProcessingService {

    @Autowired
    private DeepseekApiService deepseekApiService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Processes a PDF file and extracts information using the Deepseek API.
     * 
     * @param fileName The name of the PDF file
     * @param fileContent The content of the PDF file
     * @param prompt The prompt to send to Deepseek API
     * @return The processed PDF document with Deepseek API response
     */
    public PdfDocument processPdf(String fileName, byte[] fileContent, String prompt) {
        log.info("Starting to process PDF: {}", fileName);
        
        PdfDocument pdfDocument = PdfDocument.builder()
                .id(UUID.randomUUID().toString())
                .fileName(fileName)
                .content(fileContent)
                .fileSize(fileContent.length)
                .contentType("application/pdf")
                .status("PROCESSING")
                .processingStartTime(LocalDateTime.now().format(DATE_FORMATTER))
                .build();
        
        try {
            // Extract metadata from PDF
            extractPdfMetadata(pdfDocument);
            
            // Process with Deepseek API
            DeepseekApiResponse apiResponse = deepseekApiService.processPdf(pdfDocument, prompt);
            
            // Update document with response
            pdfDocument.setDeepseekResponse(apiResponse.getRawResponse());
            pdfDocument.setStatus(apiResponse.isHasError() ? "ERROR" : "COMPLETED");
            
        } catch (Exception e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            pdfDocument.setStatus("ERROR");
        } finally {
            pdfDocument.setProcessingEndTime(LocalDateTime.now().format(DATE_FORMATTER));
        }
        
        log.info("Finished processing PDF: {} with status: {}", fileName, pdfDocument.getStatus());
        return pdfDocument;
    }
    
    /**
     * Extracts metadata from a PDF document.
     * 
     * @param pdfDocument The PDF document
     * @throws IOException If an error occurs while reading the PDF
     */
    private void extractPdfMetadata(PdfDocument pdfDocument) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfDocument.getContent()))) {
            PDDocumentInformation info = document.getDocumentInformation();
            
            pdfDocument.setPageCount(document.getNumberOfPages());
            pdfDocument.setTitle(info.getTitle());
            pdfDocument.setAuthor(info.getAuthor());
            
            log.debug("Extracted metadata from PDF: {}, Pages: {}", 
                    pdfDocument.getFileName(), pdfDocument.getPageCount());
        }
    }
} 