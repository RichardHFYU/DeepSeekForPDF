package com.example.deepseekpdf.service;

import com.example.deepseekpdf.exception.DeepseekApiException;
import com.example.deepseekpdf.exception.DeepseekApiException.ErrorCode;
import com.example.deepseekpdf.model.DeepseekApiResponse;
import com.example.deepseekpdf.model.PdfDocument;
import io.github.pigmesh.ai.deepseek.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.chat.ChatCompletionResponse;
import io.github.pigmesh.ai.deepseek.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for interacting with the Deepseek API using the official SDK.
 */
@Service
@Slf4j
public class DeepseekApiService {

    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Value("${deepseek.api.prompt-file:classpath:prompts/default-prompt.txt}")
    private Resource promptResource;
    
    @Value("${deepseek.api.prompt:}")
    private String configPrompt;
    
    @Value("${deepseek.model:deepseek-coder}")
    private String model;
    
    @Value("${deepseek.temperature:0.7}")
    private double temperature;
    
    @Value("${deepseek.max-tokens:4096}")
    private int maxTokens;
    
    @PostConstruct
    public void init() {
        log.info("Initializing DeepseekApiService with model: {}, temperature: {}, maxTokens: {}", 
                model, temperature, maxTokens);
        validateConfiguration();
    }
    
    private void validateConfiguration() {
        if (temperature < 0 || temperature > 1) {
            throw new DeepseekApiException(
                ErrorCode.CONFIGURATION_ERROR,
                "Temperature must be between 0 and 1",
                String.format("Current value: %f", temperature)
            );
        }
        if (maxTokens <= 0) {
            throw new DeepseekApiException(
                ErrorCode.CONFIGURATION_ERROR,
                "Max tokens must be positive",
                String.format("Current value: %d", maxTokens)
            );
        }
        log.debug("Configuration validation passed successfully");
    }
    
    private String getPrompt() {
        try {
            if (!configPrompt.isEmpty()) {
                log.debug("Using configured prompt from properties");
                return configPrompt;
            }
            if (!promptResource.exists()) {
                log.warn("Prompt file not found at {}, using default prompt", promptResource.getDescription());
                return "Please analyze this PDF and provide a detailed summary.";
            }
            log.debug("Loading prompt from file: {}", promptResource.getDescription());
            return StreamUtils.copyToString(promptResource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DeepseekApiException(
                ErrorCode.PROMPT_NOT_FOUND,
                "Failed to load prompt",
                e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Creates a chat message with the PDF content and prompt.
     */
    private ChatMessage createChatMessage(PdfDocument pdfDocument, String customPrompt) {
        try {
            if (pdfDocument == null || pdfDocument.getContent() == null) {
                throw new DeepseekApiException(
                    ErrorCode.PDF_PROCESSING_ERROR,
                    "Invalid PDF document",
                    "PDF document or content is null"
                );
            }
            
            log.debug("Creating chat message for PDF: {}", pdfDocument.getFileName());
            
            // Encode PDF content as Base64
            String base64Content = Base64.getEncoder().encodeToString(pdfDocument.getContent());
            
            // Create file attachment
            Map<String, Object> fileAttachment = new HashMap<>();
            fileAttachment.put("type", "file_attachment");
            fileAttachment.put("file_type", "pdf");
            fileAttachment.put("content", base64Content);
            fileAttachment.put("name", pdfDocument.getFileName());
            
            String prompt = customPrompt != null ? customPrompt : getPrompt();
            log.trace("Using prompt: {}", prompt);
            
            // Create chat message with prompt and attachment
            return ChatMessage.builder()
                    .role("user")
                    .content(prompt)
                    .fileAttachment(fileAttachment)
                    .build();
                    
        } catch (DeepseekApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DeepseekApiException(
                ErrorCode.PDF_PROCESSING_ERROR,
                "Failed to create chat message",
                e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Creates a chat completion request with configured parameters.
     */
    private ChatCompletionRequest createCompletionRequest(ChatMessage message) {
        log.debug("Creating completion request with model: {}, temperature: {}, maxTokens: {}", 
                model, temperature, maxTokens);
                
        return ChatCompletionRequest.builder()
                .messages(Collections.singletonList(message))
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .stream(false)
                .build();
    }
    
    /**
     * Processes a PDF document through the Deepseek API with streaming support.
     * 
     * @param pdfDocument The PDF document to process
     * @param customPrompt Optional custom prompt to override default
     * @return Flux of API responses for streaming
     */
    public Flux<DeepseekApiResponse> processPdfStream(PdfDocument pdfDocument, String customPrompt) {
        String operationId = generateOperationId();
        log.info("[{}] Starting streaming PDF processing for: {}", operationId, pdfDocument.getFileName());
        
        try {
            ChatMessage message = createChatMessage(pdfDocument, customPrompt);
            ChatCompletionRequest request = createCompletionRequest(message).toBuilder()
                    .stream(true)
                    .build();
            
            AtomicReference<StringBuilder> contentBuilder = new AtomicReference<>(new StringBuilder());
            
            return deepSeekClient.chatCompletionFlux(request)
                    .map(response -> {
                        String content = response.getChoices().get("content").toString();
                        contentBuilder.get().append(content);
                        
                        log.debug("[{}] Received stream chunk, current length: {}", 
                                operationId, contentBuilder.get().length());
                        
                        return DeepseekApiResponse.builder()
                                .id(response.getId())
                                .model(response.getModel())
                                .object(response.getObject())
                                .created(response.getCreated())
                                .choices(response.getChoices())
                                .usage(response.getUsage())
                                .rawResponse(contentBuilder.get().toString())
                                .build();
                    })
                    .doOnError(e -> {
                        log.error("[{}] Error in streaming response: {}", operationId, e.getMessage(), e);
                        throw new DeepseekApiException(
                            ErrorCode.STREAM_PROCESSING_ERROR,
                            "Error during stream processing",
                            e.getMessage(),
                            e
                        );
                    })
                    .doOnComplete(() -> log.info("[{}] Completed streaming for PDF: {}", 
                            operationId, pdfDocument.getFileName()));
            
        } catch (DeepseekApiException e) {
            log.error("[{}] DeepseekApiException during streaming: {}", operationId, e.getMessage(), e);
            return Flux.error(e);
        } catch (Exception e) {
            log.error("[{}] Unexpected error during streaming: {}", operationId, e.getMessage(), e);
            return Flux.error(new DeepseekApiException(
                ErrorCode.STREAM_PROCESSING_ERROR,
                "Unexpected error during stream processing",
                e.getMessage(),
                e
            ));
        }
    }
    
    /**
     * Processes a PDF document through the Deepseek API.
     * 
     * @param pdfDocument The PDF document to process
     * @param customPrompt Optional custom prompt to override default
     * @return The API response
     */
    public DeepseekApiResponse processPdf(PdfDocument pdfDocument, String customPrompt) {
        String operationId = generateOperationId();
        log.info("[{}] Processing PDF: {}", operationId, pdfDocument.getFileName());
        
        try {
            ChatMessage message = createChatMessage(pdfDocument, customPrompt);
            ChatCompletionRequest request = createCompletionRequest(message);
            
            log.debug("[{}] Sending request to Deepseek API", operationId);
            ChatCompletionResponse response = deepSeekClient.chatCompletion(request);
            
            if (response == null || response.getChoices() == null) {
                throw new DeepseekApiException(
                    ErrorCode.INVALID_RESPONSE,
                    "Received null or invalid response from API",
                    "Response or choices is null"
                );
            }
            
            log.info("[{}] Successfully processed PDF: {}", operationId, pdfDocument.getFileName());
            log.debug("[{}] Response tokens used: {}", operationId, response.getUsage());
            
            return DeepseekApiResponse.builder()
                    .id(response.getId())
                    .model(response.getModel())
                    .object(response.getObject())
                    .created(response.getCreated())
                    .choices(response.getChoices())
                    .usage(response.getUsage())
                    .rawResponse(response.toString())
                    .build();
            
        } catch (DeepseekApiException e) {
            log.error("[{}] DeepseekApiException: {}", operationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[{}] Unexpected error: {}", operationId, e.getMessage(), e);
            throw new DeepseekApiException(
                ErrorCode.API_COMMUNICATION_ERROR,
                "Failed to process PDF",
                e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Processes a PDF document using the default prompt.
     * 
     * @param pdfDocument The PDF document to process
     * @return The API response
     */
    public DeepseekApiResponse processPdf(PdfDocument pdfDocument) {
        return processPdf(pdfDocument, null);
    }
    
    /**
     * Processes a PDF document using the default prompt with streaming support.
     * 
     * @param pdfDocument The PDF document to process
     * @return Flux of API responses for streaming
     */
    public Flux<DeepseekApiResponse> processPdfStream(PdfDocument pdfDocument) {
        return processPdfStream(pdfDocument, null);
    }
    
    /**
     * Generates a unique operation ID for tracking requests in logs.
     */
    private String generateOperationId() {
        return String.format("OP%d", System.currentTimeMillis() % 10000);
    }
} 