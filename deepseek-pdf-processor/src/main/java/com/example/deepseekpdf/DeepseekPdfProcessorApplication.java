package com.example.deepseekpdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

/**
 * Main application class for the Deepseek PDF Processor.
 * This Spring Boot application processes PDF files and extracts information
 * using the Deepseek API.
 */
@SpringBootApplication
@EnableBatchProcessing
public class DeepseekPdfProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepseekPdfProcessorApplication.class, args);
    }
} 