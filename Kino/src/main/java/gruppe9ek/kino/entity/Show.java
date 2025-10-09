package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Integer showId;

    @NotNull
    @Column(nullable = false, name = "movie_id")
    private Integer movieId;

    @NotNull
    @Column(nullable = false, name = "theater_id")
    private Integer theaterId;

    @NotNull
    @Future
    @Column(nullable = false, name = "show_datetime")
    private LocalDateTime showDatetime;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Optional staff assignments
    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "inspector_id")
    private Integer inspectorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowStatus status = ShowStatus.scheduled;



}
