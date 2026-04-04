package com.princez1.payment_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s not found with id: %s", resourceName, id));
    }
}
