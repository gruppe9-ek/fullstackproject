package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer seatId;

    @NotNull
    @Column(nullable = false, name = "row_number")
    private Integer rowNumber;

    @NotNull
    @Column(nullable = false)
    private Integer seatNumber;

    @NotNull
    @Column(nullable = false, name = "theater_id")
    private Integer theaterId;
}
