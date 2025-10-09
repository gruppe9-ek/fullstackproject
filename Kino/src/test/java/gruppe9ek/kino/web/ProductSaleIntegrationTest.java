package gruppe9ek.kino.web;

import gruppe9ek.kino.TestcontainersConfiguration;
import gruppe9ek.kino.entity.ProductSale;
import gruppe9ek.kino.repository.ProductSaleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductSaleIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductSaleRepository productSaleRepository;

    @AfterEach
    void cleanUp() {
        productSaleRepository.deleteAll();
    }

    @Test
    void testCreateProductSaleWithBooking_FullFlow() {
        // Given
        ProductSale newSale = ProductSale.builder()
                .productId(10)
                .quantity(3)
                .totalPrice(new BigDecimal("150.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        // When - POST to create
        ResponseEntity<ProductSale> createResponse = restTemplate.postForEntity(
                "/api/product-sales",
                newSale,
                ProductSale.class
        );

        // Then
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getSaleId());
        assertEquals(10, createResponse.getBody().getProductId());
        assertEquals(3, createResponse.getBody().getQuantity());
        assertEquals(new BigDecimal("150.00"), createResponse.getBody().getTotalPrice());
        assertEquals(100, createResponse.getBody().getBookingId());

        // Verify saved in database
        assertTrue(productSaleRepository.existsById(createResponse.getBody().getSaleId()));
    }

    @Test
    void testCreateProductSaleWithoutBooking_WalkIn_FullFlow() {
        // Given - Walk-in sale without booking
        ProductSale walkInSale = ProductSale.builder()
                .productId(20)
                .quantity(2)
                .totalPrice(new BigDecimal("60.00"))
                .soldById(1)
                .bookingId(null)
                .build();

        // When - POST to create
        ResponseEntity<ProductSale> createResponse = restTemplate.postForEntity(
                "/api/product-sales",
                walkInSale,
                ProductSale.class
        );

        // Then
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getSaleId());
        assertEquals(20, createResponse.getBody().getProductId());
        assertNull(createResponse.getBody().getBookingId(), "Walk-in sale should have null bookingId");

        // Verify saved in database with null bookingId
        ProductSale dbSale = productSaleRepository.findById(createResponse.getBody().getSaleId()).orElseThrow();
        assertNull(dbSale.getBookingId());
    }

    @Test
    void testGetProductSaleById_FullFlow() {
        // Given - Save product sale directly to database
        ProductSale savedSale = productSaleRepository.save(ProductSale.builder()
                .productId(30)
                .quantity(5)
                .totalPrice(new BigDecimal("250.00"))
                .soldById(2)
                .bookingId(200)
                .build());

        // When - GET by ID
        ResponseEntity<ProductSale> getResponse = restTemplate.getForEntity(
                "/api/product-sales/" + savedSale.getSaleId(),
                ProductSale.class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(savedSale.getSaleId(), getResponse.getBody().getSaleId());
        assertEquals(30, getResponse.getBody().getProductId());
        assertEquals(5, getResponse.getBody().getQuantity());
        assertEquals(new BigDecimal("250.00"), getResponse.getBody().getTotalPrice());
        assertEquals(200, getResponse.getBody().getBookingId());
    }

    @Test
    void testDeleteProductSale_FullFlow() {
        // Given - Create product sale
        ProductSale saleToDelete = productSaleRepository.save(ProductSale.builder()
                .productId(40)
                .quantity(1)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .bookingId(300)
                .build());

        Integer saleId = saleToDelete.getSaleId();
        assertTrue(productSaleRepository.existsById(saleId));

        // When - DELETE
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/product-sales/" + saleId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify removed from database
        assertFalse(productSaleRepository.existsById(saleId));
    }

    @Test
    void testGetAllProductSales_FullFlow() {
        // Given - Save multiple product sales (mix of booking and walk-in)
        productSaleRepository.save(ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build());

        productSaleRepository.save(ProductSale.builder()
                .productId(20)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .soldById(1)
                .bookingId(null) // Walk-in
                .build());

        productSaleRepository.save(ProductSale.builder()
                .productId(30)
                .quantity(3)
                .totalPrice(new BigDecimal("90.00"))
                .soldById(2)
                .bookingId(200)
                .build());

        // When - GET all
        ResponseEntity<ProductSale[]> getResponse = restTemplate.getForEntity(
                "/api/product-sales",
                ProductSale[].class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(3, getResponse.getBody().length);

        // Verify walk-in sale is included
        boolean hasWalkInSale = false;
        for (ProductSale sale : getResponse.getBody()) {
            if (sale.getBookingId() == null) {
                hasWalkInSale = true;
                break;
            }
        }
        assertTrue(hasWalkInSale, "Should include walk-in sales");
    }

    @Test
    void testGetProductSaleById_NotFound() {
        // When - GET non-existent product sale
        ResponseEntity<ProductSale> getResponse = restTemplate.getForEntity(
                "/api/product-sales/99999",
                ProductSale.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
