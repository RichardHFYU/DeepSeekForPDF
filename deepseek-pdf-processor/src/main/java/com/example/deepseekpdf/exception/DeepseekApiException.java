package com.example.deepseekpdf.exception;

import lombok.Getter;

/**
 * Custom exception for Deepseek API related errors.
 */
@Getter
public class DeepseekApiException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String details;
    
    public DeepseekApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }
    
    public DeepseekApiException(ErrorCode errorCode, String message, String details) {
        this(errorCode, message, details, null);
    }
    
    public DeepseekApiException(ErrorCode errorCode, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    /**
     * Error codes for different types of failures.
     */
    public enum ErrorCode {
        PROMPT_NOT_FOUND("DEEP_001", "Prompt file or configuration not found"),
        PDF_PROCESSING_ERROR("DEEP_002", "Error processing PDF document"),
        API_COMMUNICATION_ERROR("DEEP_003", "Error communicating with Deepseek API"),
        INVALID_RESPONSE("DEEP_004", "Invalid response from Deepseek API"),
        STREAM_PROCESSING_ERROR("DEEP_005", "Error during stream processing"),
        CONFIGURATION_ERROR("DEEP_006", "Invalid configuration");
        
        @Getter
        private final String code;
        @Getter
        private final String description;
        
        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
} 