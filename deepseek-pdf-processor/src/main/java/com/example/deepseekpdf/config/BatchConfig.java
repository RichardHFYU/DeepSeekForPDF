package com.example.deepseekpdf.config;

import com.example.deepseekpdf.job.PdfItemProcessor;
import com.example.deepseekpdf.job.PdfItemReader;
import com.example.deepseekpdf.job.PdfItemWriter;
import com.example.deepseekpdf.model.PdfDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration for PDF processing jobs.
 */
@Configuration
@Slf4j
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Value("${deepseek.batch.chunk-size:10}")
    private int chunkSize;
    
    /**
     * Configures the PDF processing job.
     * 
     * @param pdfItemReader The reader for PDF items
     * @param pdfItemProcessor The processor for PDF items
     * @param pdfItemWriter The writer for processed PDF items
     * @return The configured job
     */
    @Bean
    public Job pdfProcessingJob(PdfItemReader pdfItemReader, 
                               PdfItemProcessor pdfItemProcessor,
                               PdfItemWriter pdfItemWriter) {
        log.info("Configuring PDF processing job");
        
        return new JobBuilder("pdfProcessingJob", jobRepository)
                .start(pdfProcessingStep(pdfItemReader, pdfItemProcessor, pdfItemWriter))
                .build();
    }
    
    /**
     * Configures the PDF processing step.
     * 
     * @param pdfItemReader The reader for PDF items
     * @param pdfItemProcessor The processor for PDF items
     * @param pdfItemWriter The writer for processed PDF items
     * @return The configured step
     */
    @Bean
    public Step pdfProcessingStep(PdfItemReader pdfItemReader,
                                 PdfItemProcessor pdfItemProcessor,
                                 PdfItemWriter pdfItemWriter) {
        log.info("Configuring PDF processing step with chunk size: {}", chunkSize);
        
        return new StepBuilder("pdfProcessingStep", jobRepository)
                .<PdfDocument, PdfDocument>chunk(chunkSize, transactionManager)
                .reader(pdfItemReader)
                .processor(pdfItemProcessor)
                .writer(pdfItemWriter)
                .build();
    }
} 