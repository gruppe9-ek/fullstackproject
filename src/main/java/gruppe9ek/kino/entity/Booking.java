package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingId;

    @NotNull
    @Column(nullable = false, name = "show_id")
    private Integer showId;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String customerName;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, name = "customer_phone", length = 20)
    private String customerPhone;

    @NotNull
    @Column(nullable = false, name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Column(nullable = false, name = "sold_by_id")
    private Integer soldById;
}
