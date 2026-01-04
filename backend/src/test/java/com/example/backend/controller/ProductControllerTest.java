package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    ProductService productService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createProduct_success() throws Exception {
        ProductCreationRequest request = new ProductCreationRequest();
        request.setShopId("shop-1");
        request.setName("Test Product");
        request.setPrice(100000);
        request.setWeight(500);
        request.setBrand("Test Brand");
        request.setDescription("Test Description");
        request.setCategoryNames(Set.of("Electronics"));

        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .shopId("shop-1")
                .build();

        when(productService.createProduct(any()))
                .thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productId").value("product-1"))
                .andExpect(jsonPath("$.result.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllProductsByShopId_success() throws Exception {
        ProductResponse product1 = ProductResponse.builder()
                .productId("product-1")
                .name("Product 1")
                .build();
        ProductResponse product2 = ProductResponse.builder()
                .productId("product-2")
                .name("Product 2")
                .build();

        when(productService.getAllProducts("shop-1"))
                .thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/products/shop/shop-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getProductById_success() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .build();

        when(productService.getProductById("product-1"))
                .thenReturn(response);

        mockMvc.perform(get("/products/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productId").value("product-1"));
    }

    @Test
    void getProductById_notFound() throws Exception {
        when(productService.getProductById("non-existent"))
                .thenThrow(new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        mockMvc.perform(get("/products/non-existent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_EXIST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_EXIST.getMessage()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateProduct_success() throws Exception {
        ProductCreationRequest request = new ProductCreationRequest();
        request.setName("Updated Product");
        request.setPrice(150000);
        request.setCategoryNames(Set.of("Electronics"));

        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Updated Product")
                .price(150000)
                .build();

        when(productService.updateProduct(any(), eq("product-1")))
                .thenReturn(response);

        mockMvc.perform(put("/products/product-1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Product"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct("product-1");

        mockMvc.perform(delete("/products/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Product deleted"));
    }

    @Test
    void getProductsByCategory_success() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .productId("product-1")
                .name("Product 1")
                .build();

        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void getProductsByBrand_success() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .productId("product-1")
                .brand("Samsung")
                .build();

        when(productService.getProductsByBrand("Samsung"))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/products/brand/Samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void searchProduct_success() throws Exception {
        ProductResponse product = ProductResponse.builder()
                .productId("product-1")
                .name("iPhone")
                .build();

        when(productService.searchProduct("iPhone"))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/products/search/iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }
}