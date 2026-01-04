package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.entity.District;
import com.example.backend.entity.Province;
import com.example.backend.entity.Ward;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.repository.DistrictRepository;
import com.example.backend.repository.ProvinceRepository;
import com.example.backend.repository.WardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class LocationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    ProvinceRepository provinceRepository;

    @MockitoBean
    DistrictRepository districtRepository;

    @MockitoBean
    WardRepository wardRepository;

    @Test
    void getAllProvinces_success() throws Exception {
        Province province1 = Province.builder()
                .code("01")
                .fullName("Thành phố Hà Nội")
                .build();
        Province province2 = Province.builder()
                .code("79")
                .fullName("Thành phố Hồ Chí Minh")
                .build();

        when(provinceRepository.findAll())
                .thenReturn(List.of(province1, province2));

        mockMvc.perform(get("/locations/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("01"))
                .andExpect(jsonPath("$.result[0].fullName").value("Thành phố Hà Nội"));
    }

    @Test
    void getDistrictsByProvince_success() throws Exception {
        Province province = Province.builder()
                .code("01")
                .fullName("Thành phố Hà Nội")
                .build();

        District district1 = District.builder()
                .code("001")
                .fullName("Quận Ba Đình")
                .province(province)
                .build();
        District district2 = District.builder()
                .code("002")
                .fullName("Quận Hoàn Kiếm")
                .province(province)
                .build();

        when(districtRepository.findAllByProvince_Code("01"))
                .thenReturn(List.of(district1, district2));

        mockMvc.perform(get("/locations/districts/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("001"))
                .andExpect(jsonPath("$.result[0].fullName").value("Quận Ba Đình"));
    }

    @Test
    void getWardsByDistrict_success() throws Exception {
        Province province = Province.builder()
                .code("01")
                .fullName("Thành phố Hà Nội")
                .build();

        District district = District.builder()
                .code("001")
                .fullName("Quận Ba Đình")
                .province(province)
                .build();

        Ward ward1 = Ward.builder()
                .code("00001")
                .fullName("Phường Phúc Xá")
                .district(district)
                .build();
        Ward ward2 = Ward.builder()
                .code("00004")
                .fullName("Phường Trúc Bạch")
                .district(district)
                .build();

        when(wardRepository.findAllByDistrict_Code("001"))
                .thenReturn(List.of(ward1, ward2));

        mockMvc.perform(get("/locations/wards/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("00001"))
                .andExpect(jsonPath("$.result[0].fullName").value("Phường Phúc Xá"));
    }
}
