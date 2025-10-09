package gruppe9ek.kino.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductSaleTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testProductSaleCreation() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(3)
                .totalPrice(new BigDecimal("150.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertTrue(violations.isEmpty(), "Valid product sale should have no violations");
        assertEquals(1, productSale.getSaleId());
        assertEquals(10, productSale.getProductId());
        assertEquals(3, productSale.getQuantity());
        assertEquals(new BigDecimal("150.00"), productSale.getTotalPrice());
        assertEquals(1, productSale.getSoldById());
        assertEquals(100, productSale.getBookingId());
    }

    @Test
    void testProductIdRequired() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(null)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productId")));
    }

    @Test
    void testQuantityRequired() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(null)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    void testQuantityMinimum() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(0)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
    }

    @Test
    void testTotalPriceRequired() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(null)
                .soldById(1)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalPrice")));
    }

    @Test
    void testTotalPriceMinimum() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("-10.00"))
                .soldById(1)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalPrice")));
    }

    @Test
    void testSoldByIdRequired() {
        // Given
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(null)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("soldById")));
    }

    @Test
    void testBookingIdOptional() {
        // Given - Walk-in sale without booking
        ProductSale productSale = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("50.00"))
                .soldById(1)
                .bookingId(null)
                .build();

        // When
        Set<ConstraintViolation<ProductSale>> violations = validator.validate(productSale);

        // Then
        assertTrue(violations.isEmpty(), "Booking ID should be optional for walk-in sales");
        assertNull(productSale.getBookingId());
    }

    @Test
    void testProductSaleBuilder() {
        // Given & When
        ProductSale productSale = ProductSale.builder()
                .saleId(99)
                .productId(20)
                .quantity(5)
                .totalPrice(new BigDecimal("250.00"))
                .soldById(2)
                .bookingId(200)
                .build();

        // Then
        assertNotNull(productSale);
        assertEquals(99, productSale.getSaleId());
        assertEquals(20, productSale.getProductId());
        assertEquals(5, productSale.getQuantity());
        assertEquals(new BigDecimal("250.00"), productSale.getTotalPrice());
        assertEquals(2, productSale.getSoldById());
        assertEquals(200, productSale.getBookingId());
    }

    @Test
    void testProductSaleGettersSetters() {
        // Given
        ProductSale productSale = new ProductSale();

        // When
        productSale.setSaleId(15);
        productSale.setProductId(30);
        productSale.setQuantity(4);
        productSale.setTotalPrice(new BigDecimal("120.00"));
        productSale.setSoldById(3);
        productSale.setBookingId(300);

        // Then
        assertEquals(15, productSale.getSaleId());
        assertEquals(30, productSale.getProductId());
        assertEquals(4, productSale.getQuantity());
        assertEquals(new BigDecimal("120.00"), productSale.getTotalPrice());
        assertEquals(3, productSale.getSoldById());
        assertEquals(300, productSale.getBookingId());
    }
}
