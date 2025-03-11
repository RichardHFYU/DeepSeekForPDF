package com.example.deepseekpdf.job;

import com.example.deepseekpdf.model.PdfDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Spring Batch ItemWriter for writing processed PDF results to JSON files.
 */
@Component
@StepScope
@Slf4j
public class PdfItemWriter implements ItemWriter<PdfDocument> {

    @Value("${deepseek.pdf.output-directory}")
    private String outputDirectory;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void write(List<? extends PdfDocument> items) throws Exception {
        log.info("Writing {} processed PDF results", items.size());
        
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            log.info("Creating output directory: {}", outputDirectory);
            Files.createDirectories(outputPath);
        }
        
        for (PdfDocument document : items) {
            String outputFileName = document.getFileName().replaceAll("\\.pdf$", "") + "_result.json";
            Path outputFile = outputPath.resolve(outputFileName);
            
            // Remove content to avoid storing large binary data
            document.setContent(null);
            
            // Write result to JSON file
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(outputFile.toFile(), document);
            
            log.info("Wrote result for PDF {} to {}", document.getFileName(), outputFile);
        }
    }
} 