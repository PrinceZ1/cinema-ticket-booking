package com.princez1.customer_service.controller;

import com.princez1.common_lib.response.ApiResponse;
import com.princez1.customer_service.dto.CustomerRequest;
import com.princez1.customer_service.dto.CustomerResponse;
import com.princez1.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers()));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByEmail(email)));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByPhone(phone)));
    }

    @GetMapping("/{email}/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyCustomer(@PathVariable String email) {
        boolean verify = customerService.verifyCustomer(email);
        return ResponseEntity.ok(ApiResponse.success(verify));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest customer) {
        log.debug("POST /api/customers");
        CustomerResponse created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CustomerRequest customer) {
        return ResponseEntity.ok(ApiResponse.success(customerService.updateCustomer(id, customer)));
    }
}
