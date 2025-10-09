package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Product;
import gruppe9ek.kino.entity.ProductCategory;
import gruppe9ek.kino.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void testFindAll() {
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

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // When
        List<Product> result = productService.all();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Popcorn", result.get(0).getProductName());
        assertEquals("Cola", result.get(1).getProductName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_Empty() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Product> result = productService.all();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Given
        Product product = Product.builder()
                .productId(1)
                .productName("Popcorn")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("50.00"))
                .build();

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        // When
        Product result = productService.byId(1);

        // Then
        assertNotNull(result);
        assertEquals("Popcorn", result.getProductName());
        assertEquals(ProductCategory.popcorn, result.getCategory());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.byId(999)
        );

        assertTrue(exception.getMessage().contains("Produkt blev ikke fundet"));
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    void testCreate() {
        // Given
        Product productToCreate = Product.builder()
                .productId(10) // Should be ignored
                .productName("New Product")
                .category(ProductCategory.candy)
                .price(new BigDecimal("30.00"))
                .build();

        Product savedProduct = Product.builder()
                .productId(1) // Assigned by database
                .productName("New Product")
                .category(ProductCategory.candy)
                .price(new BigDecimal("30.00"))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        Product result = productService.create(productToCreate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("New Product", result.getProductName());
        verify(productRepository, times(1)).save(any(Product.class));
        // Verify ID was set to null before save
        assertNull(productToCreate.getProductId());
    }

    @Test
    void testCreate_NullId() {
        // Given
        Product productToCreate = Product.builder()
                .productId(99)
                .productName("Test")
                .category(ProductCategory.other)
                .price(new BigDecimal("10.00"))
                .build();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertNull(p.getProductId(), "ID should be null before save");
            return p;
        });

        // When
        productService.create(productToCreate);

        // Then
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdate_Success() {
        // Given
        Product existingProduct = Product.builder()
                .productId(1)
                .productName("Old Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("40.00"))
                .build();

        Product updatedProduct = Product.builder()
                .productName("Updated Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("45.00"))
                .build();

        when(productRepository.existsById(1)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.update(1, updatedProduct);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getProductName());
        assertEquals(new BigDecimal("45.00"), result.getPrice());
        assertEquals(1, updatedProduct.getProductId()); // ID should be set
        verify(productRepository, times(1)).existsById(1);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        Product updatedProduct = Product.builder()
                .productName("Updated Name")
                .category(ProductCategory.popcorn)
                .price(new BigDecimal("45.00"))
                .build();

        when(productRepository.existsById(999)).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.update(999, updatedProduct)
        );

        assertTrue(exception.getMessage().contains("Produkt blev ikke fundet"));
        verify(productRepository, times(1)).existsById(999);
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdate_PreservesId() {
        // Given
        Product updatedProduct = Product.builder()
                .productName("Updated")
                .category(ProductCategory.candy)
                .price(new BigDecimal("20.00"))
                .build();

        when(productRepository.existsById(5)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            assertEquals(5, p.getProductId(), "ID should be set to 5");
            return p;
        });

        // When
        productService.update(5, updatedProduct);

        // Then
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDelete() {
        // Given
        Integer productId = 1;

        // When
        productService.delete(productId);

        // Then
        verify(productRepository, times(1)).deleteById(productId);
    }
}
