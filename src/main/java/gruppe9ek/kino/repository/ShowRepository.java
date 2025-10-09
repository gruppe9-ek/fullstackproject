
package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Show;
import gruppe9ek.kino.entity.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Integer> {
    /**
     * Spring Data derived query.
     * Counts how many Show rows match the given movieId AND status.
     * Equivalent SQL:
     *   SELECT COUNT(*) FROM shows WHERE movie_id = ? AND status = ?;
     * Used to block deleting a movie that still has scheduled shows.
     */
    long countByMovieIdAndStatus(Integer movieId, ShowStatus status);

}
