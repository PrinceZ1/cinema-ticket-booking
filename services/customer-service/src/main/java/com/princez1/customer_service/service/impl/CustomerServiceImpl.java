package com.princez1.customer_service.service.impl;

import com.princez1.customer_service.dto.CustomerRequest;
import com.princez1.customer_service.dto.CustomerResponse;
import com.princez1.customer_service.entity.Customer;
import com.princez1.customer_service.exception.DuplicateResourceException;
import com.princez1.customer_service.exception.ResourceNotFoundException;
import com.princez1.customer_service.repository.CustomerRepository;
import com.princez1.customer_service.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.debug("createCustomer()");

        Customer customer = toEntity(request);
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("Customer email already exists: " + customer.getEmail());
        }
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new DuplicateResourceException("Customer phone already exists: " + customer.getPhone());
        }

        if (customer.getStatus() == null) {
            customer.setStatus(Customer.CustomerStatus.ACTIVE);
        }

        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CustomerResponse getCustomerById(Long id) {
        log.debug("getCustomerById(id={})", id);
        return toResponse(
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", String.valueOf(id))));
    }

    @Override
    @Transactional
    public CustomerResponse getCustomerByEmail(String email) {
        log.debug("getCustomerByEmail(email={})", email);
        return toResponse(
                customerRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email)));
    }

    @Override
    @Transactional
    public CustomerResponse getCustomerByPhone(String phone) {
        log.debug("getCustomerByPhone(phone={})", phone);
        return toResponse(
                customerRepository
                        .findByPhone(phone)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", "phone", phone)));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.debug("updateCustomer(id={})", id);

        Customer existing =
                customerRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", String.valueOf(id)));

        if (request.getEmail() != null && !request.getEmail().equals(existing.getEmail())) {
            Optional<Customer> byEmail = customerRepository.findByEmail(request.getEmail());
            if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
                throw new DuplicateResourceException(
                        "Customer email already exists: " + request.getEmail());
            }
        }
        if (request.getPhone() != null && !request.getPhone().equals(existing.getPhone())) {
            Optional<Customer> byPhone = customerRepository.findByPhone(request.getPhone());
            if (byPhone.isPresent() && !byPhone.get().getId().equals(id)) {
                throw new DuplicateResourceException(
                        "Customer phone already exists: " + request.getPhone());
            }
        }

        existing.setFullName(
                request.getFullName() != null ? request.getFullName() : existing.getFullName());
        existing.setEmail(request.getEmail() != null ? request.getEmail() : existing.getEmail());
        existing.setPhone(request.getPhone() != null ? request.getPhone() : existing.getPhone());

        return toResponse(customerRepository.save(existing));
    }

    @Override
    @Transactional
    public boolean verifyCustomer(String email) {
        log.debug("verifyCustomer(email={})", email);
        return customerRepository
                .findByEmail(email)
                .map(c -> c.getStatus() == Customer.CustomerStatus.ACTIVE)
                .orElse(false);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(), customer.getFullName(), customer.getPhone(), customer.getEmail());
    }

    private Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        return customer;
    }
}

