package com.example.userquotaservice.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service responsible for creating standardized response objects
 * Used to ensure consistent response format across all controllers
 */
@Service
public class ResponseService {

    /**
     * Creates a standardized success response
     * 
     * @param data The data to include in the response
     * @param message A descriptive message about the operation
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> success(Object data, String message) {
        return wrapResponse(HttpStatus.OK.value(), "Success", message, data);
    }
    
    /**
     * Creates a standardized mock operation response
     * 
     * @param data The mock data to include in the response
     * @param operation Description of the operation being mocked
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> mockOperation(Object data, String operation) {
        return wrapResponse(
            HttpStatus.OK.value(),
            "Mock Operation",
            operation + " (mock operation - no database access)",
            data
        );
    }
    
    /**
     * Creates a standardized error response
     * 
     * @param status The HTTP status code
     * @param errorType A short error type description
     * @param message A detailed error message
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> error(HttpStatus status, String errorType, String message) {
        return ResponseEntity.status(status)
                .body(createResponseBody(status.value(), errorType, message, null));
    }
    
    /**
     * Creates a standardized not found error response
     * 
     * @param resourceType Type of resource not found (e.g., "User", "Resource")
     * @param details Details about what was not found
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> notFound(String resourceType, String details) {
        return error(
            HttpStatus.NOT_FOUND,
            resourceType + " Not Found",
            details
        );
    }
    
    /**
     * Creates a standardized bad request error response
     * 
     * @param errorType Type of validation error
     * @param details Details about the validation error
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> badRequest(String errorType, String details) {
        return error(
            HttpStatus.BAD_REQUEST,
            errorType,
            details
        );
    }
    
    /**
     * Creates a standardized internal server error response
     * 
     * @param message Error message
     * @return A ResponseEntity with standardized structure
     */
    public ResponseEntity<?> internalError(String message) {
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred while processing your request: " + message
        );
    }
    
    /**
     * Helper method to wrap a response with the standard format
     */
    private ResponseEntity<?> wrapResponse(int status, String error, String message, Object data) {
        return ResponseEntity.status(HttpStatus.valueOf(status))
                .body(createResponseBody(status, error, message, data));
    }
    
    /**
     * Create a standardized response body
     */
    private Map<String, Object> createResponseBody(int status, String error, String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        if (data != null) {
            body.put("data", data);
        }
        return body;
    }
} 