package gruppe9ek.kino.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import gruppe9ek.kino.entity.ProductSale;
import gruppe9ek.kino.service.ProductSaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(ProductSaleController.class)
class ProductSaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductSaleService productSaleService;

    @Test
    void testGetAll_ReturnsProductSales() throws Exception {
        // Given
        ProductSale sale1 = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        ProductSale sale2 = ProductSale.builder()
                .saleId(2)
                .productId(20)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .soldById(1)
                .bookingId(null) // Walk-in sale
                .build();

        when(productSaleService.all()).thenReturn(Arrays.asList(sale1, sale2));

        // When & Then
        mockMvc.perform(get("/api/product-sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value(10))
                .andExpect(jsonPath("$[1].productId").value(20));

        verify(productSaleService, times(1)).all();
    }

    @Test
    void testGetAll_Empty() throws Exception {
        // Given
        when(productSaleService.all()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/product-sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productSaleService, times(1)).all();
    }

    @Test
    void testGetById_Success() throws Exception {
        // Given
        ProductSale sale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(3)
                .totalPrice(new BigDecimal("150.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        when(productSaleService.byId(1)).thenReturn(sale);

        // When & Then
        mockMvc.perform(get("/api/product-sales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(150.00));

        verify(productSaleService, times(1)).byId(1);
    }

    @Test
    void testGetById_NotFound() throws Exception {
        // Given
        when(productSaleService.byId(999)).thenThrow(new ResponseStatusException(NOT_FOUND, "Produktsalg blev ikke fundet"));

        // When & Then
        mockMvc.perform(get("/api/product-sales/999"))
                .andExpect(status().isNotFound());

        verify(productSaleService, times(1)).byId(999);
    }

    @Test
    void testCreate_Success() throws Exception {
        // Given
        ProductSale newSale = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        ProductSale savedSale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        when(productSaleService.create(any(ProductSale.class))).thenReturn(savedSale);

        // When & Then
        mockMvc.perform(post("/api/product-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSale)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(productSaleService, times(1)).create(any(ProductSale.class));
    }

    @Test
    void testCreate_WithBookingId() throws Exception {
        // Given
        ProductSale saleWithBooking = ProductSale.builder()
                .productId(10)
                .quantity(1)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .bookingId(200)
                .build();

        ProductSale savedSale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(1)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .bookingId(200)
                .build();

        when(productSaleService.create(any(ProductSale.class))).thenReturn(savedSale);

        // When & Then
        mockMvc.perform(post("/api/product-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleWithBooking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(200));

        verify(productSaleService, times(1)).create(any(ProductSale.class));
    }

    @Test
    void testCreate_WithoutBookingId() throws Exception {
        // Given - Walk-in sale without booking
        ProductSale walkInSale = ProductSale.builder()
                .productId(10)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .soldById(1)
                .bookingId(null)
                .build();

        ProductSale savedSale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .soldById(1)
                .bookingId(null)
                .build();

        when(productSaleService.create(any(ProductSale.class))).thenReturn(savedSale);

        // When & Then
        mockMvc.perform(post("/api/product-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walkInSale)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").doesNotExist());

        verify(productSaleService, times(1)).create(any(ProductSale.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        // Given
        doNothing().when(productSaleService).delete(1);

        // When & Then
        mockMvc.perform(delete("/api/product-sales/1"))
                .andExpect(status().isOk());

        verify(productSaleService, times(1)).delete(1);
    }
}
