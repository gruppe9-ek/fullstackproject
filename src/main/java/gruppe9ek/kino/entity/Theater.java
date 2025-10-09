package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "theaters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theater_id")
    private Integer theaterId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "theater_name", nullable = false, unique = true, length = 50)
    private String theaterName;

    @NotNull
    @Positive
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;

    @NotNull
    @Positive
    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;
}
