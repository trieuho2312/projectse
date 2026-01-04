package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.ShopService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ShopControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    ShopService shopService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void createShop_success() throws Exception {
        AddressDTO address = AddressDTO.builder()
                .name("Test Shop")
                .phone("0123456789")
                .addressDetail("123 Test Street")
                .wardCode("001")
                .build();

        ShopCreationRequest request = ShopCreationRequest.builder()
                .name("My Shop")
                .address(address)
                .build();

        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .ownerUsername("user1")
                .build();

        when(shopService.createShop(any()))
                .thenReturn(response);

        mockMvc.perform(post("/shops")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.shopId").value("shop-1"))
                .andExpect(jsonPath("$.result.name").value("My Shop"));
    }

    @Test
    void getAllShops_success() throws Exception {
        ShopResponse shop1 = ShopResponse.builder()
                .shopId("shop-1")
                .name("Shop 1")
                .build();
        ShopResponse shop2 = ShopResponse.builder()
                .shopId("shop-2")
                .name("Shop 2")
                .build();

        when(shopService.getAllShop())
                .thenReturn(List.of(shop1, shop2));

        mockMvc.perform(get("/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getShopById_success() throws Exception {
        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .build();

        when(shopService.getShopById("shop-1"))
                .thenReturn(response);

        mockMvc.perform(get("/shops/shop-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value("shop-1"));
    }

    @Test
    void getShopById_notFound() throws Exception {
        when(shopService.getShopById("non-existent"))
                .thenThrow(new AppException(ErrorCode.SHOP_NOT_EXIST));

        mockMvc.perform(get("/shops/non-existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SHOP_NOT_EXIST.getCode()));
    }

    @Test
    void getShopByOwnerId_success() throws Exception {
        ShopResponse shop = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .ownerUsername("user1")
                .build();

        when(shopService.getAllShopByOwnerId("user1"))
                .thenReturn(List.of(shop));

        mockMvc.perform(get("/shops/owner/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void searchShops_byDistrictAndProvince() throws Exception {
        ShopResponse shop = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .build();

        when(shopService.getShopsByDistrictAndProvince("District 1", "Ho Chi Minh"))
                .thenReturn(List.of(shop));

        mockMvc.perform(get("/shops/search")
                        .param("district", "District 1")
                        .param("province", "Ho Chi Minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void searchShops_byDistrict() throws Exception {
        ShopResponse shop = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .build();

        when(shopService.getShopsByDistrict("District 1"))
                .thenReturn(List.of(shop));

        mockMvc.perform(get("/shops/search")
                        .param("district", "District 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void searchShops_byProvince() throws Exception {
        ShopResponse shop = ShopResponse.builder()
                .shopId("shop-1")
                .name("My Shop")
                .build();

        when(shopService.getShopsByProvince("Ho Chi Minh"))
                .thenReturn(List.of(shop));

        mockMvc.perform(get("/shops/search")
                        .param("province", "Ho Chi Minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }
}
