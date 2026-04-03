package com.princez1.customer_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princez1.customer_service.dto.CustomerRequest;
import com.princez1.customer_service.dto.CustomerResponse;
import com.princez1.customer_service.exception.GlobalExceptionHandler;
import com.princez1.customer_service.exception.ResourceNotFoundException;
import com.princez1.customer_service.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private CustomerService customerService;

    @Test
    void testCreateCustomer_validInput_returns201() throws Exception {
        CustomerRequest request = new CustomerRequest("John Doe", "123456789", "john@example.com");

        CustomerResponse created =
                new CustomerResponse(1L, request.getFullName(), request.getPhone(), request.getEmail());

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(created);

        mockMvc
                .perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetCustomerByEmail_notFound_returns404() throws Exception {
        when(customerService.getCustomerByEmail("missing@example.com"))
                .thenThrow(new ResourceNotFoundException("Customer", "email", "missing@example.com"));

        mockMvc
                .perform(get("/api/customers/email/missing@example.com").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testVerifyCustomer_activeCustomer_returnsTrue() throws Exception {
        when(customerService.verifyCustomer("test@example.com")).thenReturn(true);

        mockMvc
                .perform(
                        get("/api/customers/{email}/verify", "test@example.com")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}


