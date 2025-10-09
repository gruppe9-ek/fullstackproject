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

class ProductTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testProductCreation() {
        // Given
        Product product = Product.builder()
                .productId(1)
                .productName("Popcorn Stor")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("50.00"))
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertTrue(violations.isEmpty(), "Valid product should have no violations");
        assertEquals(1, product.getProductId());
        assertEquals("Popcorn Stor", product.getProductName());
        assertEquals(ProductCategory.popcorn, product.getCategory());
        assertEquals(new BigDecimal("50.00"), product.getPrice());
    }

    @Test
    void testProductNameRequired() {
        // Given
        Product product = Product.builder()
                .productName(null)
                .category(ProductCategory.candy)
                .price(new BigDecimal("25.00"))
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productName")));
    }

    @Test
    void testProductNameNotBlank() {
        // Given
        Product product = Product.builder()
                .productName("")
                .category(ProductCategory.candy)
                .price(new BigDecimal("25.00"))
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("productName")));
    }

    @Test
    void testCategoryRequired() {
        // Given
        Product product = Product.builder()
                .productName("Test Product")
                .category(null)
                .price(new BigDecimal("25.00"))
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("category")));
    }

    @Test
    void testPriceRequired() {
        // Given
        Product product = Product.builder()
                .productName("Test Product")
                .category(ProductCategory.soda)
                .price(null)
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void testPriceMinimum() {
        // Given
        Product product = Product.builder()
                .productName("Test Product")
                .category(ProductCategory.soda)
                .price(new BigDecimal("-10.00"))
                .build();

        // When
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

    @Test
    void testAllCategories() {
        // Test all enum categories
        ProductCategory[] categories = {
                ProductCategory.candy,
                ProductCategory.soda,
                ProductCategory.popcorn,
                ProductCategory.other
        };

        for (ProductCategory category : categories) {
            Product product = Product.builder()
                    .productName("Test " + category)
                    .category(category)
                    .price(new BigDecimal("10.00"))
                    .build();

            Set<ConstraintViolation<Product>> violations = validator.validate(product);
            assertTrue(violations.isEmpty(), "Category " + category + " should be valid");
        }
    }

    @Test
    void testProductBuilder() {
        // Given & When
        Product product = Product.builder()
                .productId(10)
                .productName("Builder Test")
                .category(ProductCategory.other)
                .price(new BigDecimal("99.99"))
                .build();

        // Then
        assertNotNull(product);
        assertEquals(10, product.getProductId());
        assertEquals("Builder Test", product.getProductName());
        assertEquals(ProductCategory.other, product.getCategory());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
    }

    @Test
    void testProductGettersSetters() {
        // Given
        Product product = new Product();

        // When
        product.setProductId(5);
        product.setProductName("Getter Setter Test");
        product.setCategory(ProductCategory.popcorn);
        product.setPrice(new BigDecimal("35.50"));

        // Then
        assertEquals(5, product.getProductId());
        assertEquals("Getter Setter Test", product.getProductName());
        assertEquals(ProductCategory.popcorn, product.getCategory());
        assertEquals(new BigDecimal("35.50"), product.getPrice());
    }
}
