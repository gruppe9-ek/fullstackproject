package gruppe9ek.kino.web;

import gruppe9ek.kino.TestcontainersConfiguration;
import gruppe9ek.kino.entity.Product;
import gruppe9ek.kino.entity.ProductCategory;
import gruppe9ek.kino.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    void testCreateProduct_FullFlow() {
        // Given
        Product newProduct = Product.builder()
                .productName("Integration Popcorn")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("55.00"))
                .build();

        // When - POST to create
        ResponseEntity<Product> createResponse = restTemplate.postForEntity(
                "/api/products",
                newProduct,
                Product.class
        );

        // Then
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getProductId());
        assertEquals("Integration Popcorn", createResponse.getBody().getProductName());

        // Verify saved in database
        assertTrue(productRepository.existsById(createResponse.getBody().getProductId()));
    }

    @Test
    void testGetProductById_FullFlow() {
        // Given - Save product directly to database
        Product savedProduct = productRepository.save(Product.builder()
                .productName("Test Cola")
                .category(ProductCategory.soda)
                .price(new BigDecimal("30.00"))
                .build());

        // When - GET by ID
        ResponseEntity<Product> getResponse = restTemplate.getForEntity(
                "/api/products/" + savedProduct.getProductId(),
                Product.class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(savedProduct.getProductId(), getResponse.getBody().getProductId());
        assertEquals("Test Cola", getResponse.getBody().getProductName());
        assertEquals(ProductCategory.soda, getResponse.getBody().getCategory());
    }

    @Test
    void testUpdateProduct_FullFlow() {
        // Given - Create product
        Product existingProduct = productRepository.save(Product.builder()
                .productName("Old Name")
                .category(ProductCategory.candy)
                .price(new BigDecimal("20.00"))
                .build());

        Product updatedProduct = Product.builder()
                .productName("New Name")
                .category(ProductCategory.candy)
                .price(new BigDecimal("25.00"))
                .build();

        // When - PUT to update
        ResponseEntity<Product> updateResponse = restTemplate.exchange(
                "/api/products/" + existingProduct.getProductId(),
                HttpMethod.PUT,
                new HttpEntity<>(updatedProduct),
                Product.class
        );

        // Then
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("New Name", updateResponse.getBody().getProductName());
        assertEquals(new BigDecimal("25.00"), updateResponse.getBody().getPrice());

        // Verify updated in database
        Product dbProduct = productRepository.findById(existingProduct.getProductId()).orElseThrow();
        assertEquals("New Name", dbProduct.getProductName());
        assertEquals(new BigDecimal("25.00"), dbProduct.getPrice());
    }

    @Test
    void testDeleteProduct_FullFlow() {
        // Given - Create product
        Product productToDelete = productRepository.save(Product.builder()
                .productName("To Delete")
                .category(ProductCategory.other)
                .price(new BigDecimal("15.00"))
                .build());

        Integer productId = productToDelete.getProductId();
        assertTrue(productRepository.existsById(productId));

        // When - DELETE
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/products/" + productId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Then
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        // Verify removed from database
        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void testGetAllProducts_FullFlow() {
        // Given - Save multiple products
        productRepository.save(Product.builder()
                .productName("Product 1")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("40.00"))
                .build());

        productRepository.save(Product.builder()
                .productName("Product 2")
                .category(ProductCategory.soda)
                .price(new BigDecimal("30.00"))
                .build());

        productRepository.save(Product.builder()
                .productName("Product 3")
                .category(ProductCategory.candy)
                .price(new BigDecimal("20.00"))
                .build());

        // When - GET all
        ResponseEntity<Product[]> getResponse = restTemplate.getForEntity(
                "/api/products",
                Product[].class
        );

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(3, getResponse.getBody().length);
    }

    @Test
    void testGetProductById_NotFound() {
        // When - GET non-existent product
        ResponseEntity<Product> getResponse = restTemplate.getForEntity(
                "/api/products/99999",
                Product.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
