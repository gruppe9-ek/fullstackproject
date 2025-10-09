package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer saleId;

    @NotNull
    @Column(nullable = false, name = "product_id")
    private Integer productId;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Column(nullable = false, name = "sold_by_id")
    private Integer soldById;

    @Column(name = "booking_id")
    private Integer bookingId;
}
