package com.princez1.customer_service.service;

import com.princez1.customer_service.dto.CustomerRequest;
import com.princez1.customer_service.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest customer);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByEmail(String email);

    CustomerResponse getCustomerByPhone(String phone);

    CustomerResponse updateCustomer(Long id, CustomerRequest customer);

    boolean verifyCustomer(String email);
}
