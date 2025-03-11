package com.example.deepseekpdf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a response from the Deepseek API.
 * This class can be extended based on the actual Deepseek API response structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepseekApiResponse {
    
    private String id;
    private String model;
    private String object;
    private Long created;
    private Map<String, Object> choices;
    private Map<String, Object> usage;
    private String rawResponse;
    
    // Error handling
    private boolean hasError;
    private String errorMessage;
    private String errorCode;
} 