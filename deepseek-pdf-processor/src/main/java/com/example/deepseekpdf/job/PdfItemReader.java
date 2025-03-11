package com.example.deepseekpdf.job;

import com.example.deepseekpdf.model.PdfDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Batch ItemReader for reading PDF files from a directory.
 */
@Component
@StepScope
@Slf4j
public class PdfItemReader implements ItemReader<PdfDocument> {

    @Value("${deepseek.pdf.input-directory}")
    private String inputDirectory;
    
    private List<File> pdfFiles;
    private AtomicInteger nextFileIndex;
    
    /**
     * Initializes the reader by scanning the input directory for PDF files.
     * 
     * @throws Exception If an error occurs during initialization
     */
    public void init() throws Exception {
        log.info("Initializing PDF item reader with input directory: {}", inputDirectory);
        
        Path inputPath = Paths.get(inputDirectory);
        if (!Files.exists(inputPath)) {
            log.warn("Input directory does not exist: {}", inputDirectory);
            Files.createDirectories(inputPath);
            log.info("Created input directory: {}", inputDirectory);
        }
        
        try (Stream<Path> paths = Files.walk(inputPath)) {
            pdfFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        
        log.info("Found {} PDF files in directory: {}", pdfFiles.size(), inputDirectory);
        nextFileIndex = new AtomicInteger(0);
    }
    
    @Override
    public PdfDocument read() throws Exception {
        if (pdfFiles == null) {
            init();
        }
        
        int index = nextFileIndex.getAndIncrement();
        if (index < pdfFiles.size()) {
            File pdfFile = pdfFiles.get(index);
            log.info("Reading PDF file {}/{}: {}", index + 1, pdfFiles.size(), pdfFile.getName());
            
            try {
                byte[] content = Files.readAllBytes(pdfFile.toPath());
                
                return PdfDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .fileName(pdfFile.getName())
                        .filePath(pdfFile.getAbsolutePath())
                        .content(content)
                        .fileSize(content.length)
                        .contentType("application/pdf")
                        .status("PENDING")
                        .build();
            } catch (IOException e) {
                log.error("Error reading PDF file: {}", pdfFile.getName(), e);
                throw e;
            }
        }
        
        return null; // No more items
    }
} 