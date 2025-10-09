package gruppe9ek.kino.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import gruppe9ek.kino.entity.Product;
import gruppe9ek.kino.entity.ProductCategory;
import gruppe9ek.kino.service.ProductService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void testGetAll_ReturnsProducts() throws Exception {
        // Given
        Product product1 = Product.builder()
                .productId(1)
                .productName("Popcorn")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("50.00"))
                .build();

        Product product2 = Product.builder()
                .productId(2)
                .productName("Cola")
                .category(ProductCategory.soda)
                .price(new BigDecimal("25.00"))
                .build();

        when(productService.all()).thenReturn(Arrays.asList(product1, product2));

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productName").value("Popcorn"))
                .andExpect(jsonPath("$[1].productName").value("Cola"));

        verify(productService, times(1)).all();
    }

    @Test
    void testGetAll_Empty() throws Exception {
        // Given
        when(productService.all()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(productService, times(1)).all();
    }

    @Test
    void testGetById_Success() throws Exception {
        // Given
        Product product = Product.builder()
                .productId(1)
                .productName("Popcorn")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("50.00"))
                .build();

        when(productService.byId(1)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Popcorn"))
                .andExpect(jsonPath("$.category").value("popcorn"))
                .andExpect(jsonPath("$.price").value(50.00));

        verify(productService, times(1)).byId(1);
    }

    @Test
    void testGetById_NotFound() throws Exception {
        // Given
        when(productService.byId(999)).thenThrow(new ResponseStatusException(NOT_FOUND, "Produkt blev ikke fundet"));

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).byId(999);
    }

    @Test
    void testCreate_Success() throws Exception {
        // Given
        Product newProduct = Product.builder()
                .productName("Slik Mix")
                .category(ProductCategory.candy)
                .price(new BigDecimal("35.00"))
                .build();

        Product savedProduct = Product.builder()
                .productId(1)
                .productName("Slik Mix")
                .category(ProductCategory.candy)
                .price(new BigDecimal("35.00"))
                .build();

        when(productService.create(any(Product.class))).thenReturn(savedProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Slik Mix"));

        verify(productService, times(1)).create(any(Product.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        // Given
        Product updated = Product.builder()
                .productName("Updated Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("45.00"))
                .build();

        Product savedProduct = Product.builder()
                .productId(1)
                .productName("Updated Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("45.00"))
                .build();

        when(productService.update(eq(1), any(Product.class))).thenReturn(savedProduct);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Name"))
                .andExpect(jsonPath("$.price").value(45.00));

        verify(productService, times(1)).update(eq(1), any(Product.class));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        // Given
        Product updated = Product.builder()
                .productName("Updated Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("45.00"))
                .build();

        when(productService.update(eq(999), any(Product.class)))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Produkt blev ikke fundet"));

        // When & Then
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).update(eq(999), any(Product.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        // Given
        doNothing().when(productService).delete(1);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());

        verify(productService, times(1)).delete(1);
    }
}
