package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String message;
    private Map<String, String> errors;

    public ErrorResponseDTO(int status, String message) {
        this(status, message, null);
    }
}
