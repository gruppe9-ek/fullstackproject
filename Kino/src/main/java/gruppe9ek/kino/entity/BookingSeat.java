package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "booking_seats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingSeatId;

    @NotNull
    @Column(nullable = false, name = "booking_id")
    private Integer bookingId;

    @NotNull
    @Column(nullable = false, name = "seat_id")
    private Integer seatId;
}
