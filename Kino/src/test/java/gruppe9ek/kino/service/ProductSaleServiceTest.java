package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.ProductSale;
import gruppe9ek.kino.repository.ProductSaleRepository;
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
class ProductSaleServiceTest {

    @Mock
    private ProductSaleRepository productSaleRepository;

    @InjectMocks
    private ProductSaleService productSaleService;

    @Test
    void testFindAll() {
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

        when(productSaleRepository.findAll()).thenReturn(Arrays.asList(sale1, sale2));

        // When
        List<ProductSale> result = productSaleService.all();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getProductId());
        assertEquals(20, result.get(1).getProductId());
        assertNotNull(result.get(0).getBookingId());
        assertNull(result.get(1).getBookingId());
        verify(productSaleRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_Empty() {
        // Given
        when(productSaleRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<ProductSale> result = productSaleService.all();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productSaleRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        // Given
        ProductSale sale = ProductSale.builder()
                .saleId(1)
                .productId(10)
                .quantity(3)
                .totalPrice(new BigDecimal("150.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        when(productSaleRepository.findById(1)).thenReturn(Optional.of(sale));

        // When
        ProductSale result = productSaleService.byId(1);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getProductId());
        assertEquals(3, result.getQuantity());
        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
        verify(productSaleRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(productSaleRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productSaleService.byId(999)
        );

        assertTrue(exception.getMessage().contains("Produktsalg blev ikke fundet"));
        verify(productSaleRepository, times(1)).findById(999);
    }

    @Test
    void testCreate() {
        // Given
        ProductSale saleToCreate = ProductSale.builder()
                .saleId(99) // Should be ignored
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        ProductSale savedSale = ProductSale.builder()
                .saleId(1) // Assigned by database
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(100)
                .build();

        when(productSaleRepository.save(any(ProductSale.class))).thenReturn(savedSale);

        // When
        ProductSale result = productSaleService.create(saleToCreate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getSaleId());
        assertEquals(10, result.getProductId());
        assertEquals(2, result.getQuantity());
        verify(productSaleRepository, times(1)).save(any(ProductSale.class));
        // Verify ID was set to null before save
        assertNull(saleToCreate.getSaleId());
    }

    @Test
    void testCreate_WithBookingId() {
        // Given
        ProductSale saleWithBooking = ProductSale.builder()
                .productId(10)
                .quantity(2)
                .totalPrice(new BigDecimal("100.00"))
                .soldById(1)
                .bookingId(200)
                .build();

        when(productSaleRepository.save(any(ProductSale.class))).thenReturn(saleWithBooking);

        // When
        ProductSale result = productSaleService.create(saleWithBooking);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getBookingId());
        verify(productSaleRepository, times(1)).save(any(ProductSale.class));
    }

    @Test
    void testCreate_WithoutBookingId() {
        // Given - Walk-in sale without booking
        ProductSale walkInSale = ProductSale.builder()
                .productId(10)
                .quantity(1)
                .totalPrice(new BigDecimal("25.00"))
                .soldById(1)
                .bookingId(null)
                .build();

        when(productSaleRepository.save(any(ProductSale.class))).thenReturn(walkInSale);

        // When
        ProductSale result = productSaleService.create(walkInSale);

        // Then
        assertNotNull(result);
        assertNull(result.getBookingId());
        verify(productSaleRepository, times(1)).save(any(ProductSale.class));
    }

    @Test
    void testDelete() {
        // Given
        Integer saleId = 1;

        // When
        productSaleService.delete(saleId);

        // Then
        verify(productSaleRepository, times(1)).deleteById(saleId);
    }
}
