package com.example.deepseekpdf.job;

import com.example.deepseekpdf.model.PdfDocument;
import com.example.deepseekpdf.service.PdfProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring Batch ItemProcessor for processing PDF files with the Deepseek API.
 */
@Component
@StepScope
@Slf4j
public class PdfItemProcessor implements ItemProcessor<PdfDocument, PdfDocument> {

    @Autowired
    private PdfProcessingService pdfProcessingService;
    
    @Value("${deepseek.api.prompt}")
    private String defaultPrompt;
    
    @Override
    public PdfDocument process(PdfDocument pdfDocument) throws Exception {
        log.info("Processing PDF: {}", pdfDocument.getFileName());
        
        try {
            // Use the default prompt from configuration
            return pdfProcessingService.processPdf(
                    pdfDocument.getFileName(),
                    pdfDocument.getContent(),
                    defaultPrompt
            );
        } catch (Exception e) {
            log.error("Error processing PDF: {}", pdfDocument.getFileName(), e);
            throw e;
        }
    }
} 