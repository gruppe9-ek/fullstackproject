// src/main/java/gruppe9ek/kino/entity/Movie.java
package gruppe9ek.kino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "movies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String category;

    @NotNull
    @Min(0)
    @Column(name = "age_limit", nullable = false)
    private Integer ageLimit;

    @NotNull
    @Positive
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // Lob betyder large OBject, det er indsat her da en tekst kan blive lang fx (biografbeskrivelser, store lister af actors)
    @Lob private String actors;
    @Lob private String description;
}






