package com.example.Identity.Reconciliation.DTO;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ErrorResponse{
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}