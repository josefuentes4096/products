package com.josefuentes4096.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String mensaje;
    private Map<String, String> errores;

    public ErrorResponseDTO(int status, String mensaje) {
        this(status, mensaje, null);
    }
}
