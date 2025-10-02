package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String customerName;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "booking_datetime", nullable = false)
    private LocalDateTime bookingDatetime = LocalDateTime.now();
}
