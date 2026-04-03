package com.princez1.customer_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s not found with id: %s", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String identifierName, String value) {
        super(String.format("%s not found with %s: %s", resourceName, identifierName, value));
    }
}

