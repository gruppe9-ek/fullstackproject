package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {}
