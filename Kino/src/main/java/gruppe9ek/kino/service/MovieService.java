package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Movie;
import gruppe9ek.kino.entity.ShowStatus;
import gruppe9ek.kino.repository.MovieRepository;
import gruppe9ek.kino.repository.ShowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class MovieService {
    private static final Set<Integer> ALLOWED_AGE_LIMITS = Set.of(0, 7, 11, 15, 18);
    private final MovieRepository movies;
    private final ShowRepository shows;

    public MovieService(MovieRepository movies, ShowRepository shows) {
        this.movies = movies;
        this.shows = shows;
    }

    public List<Movie> all() { return movies.findAll(); }

    public Movie byId(Integer id) {
        return movies.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    public Movie create(Movie m) {
        // Bean Validation (@Valid) runs in controller; we keep the set-membership rule here:
        validateAgeLimit(m.getAgeLimit());
        m.setMovieId(null);
        return movies.save(m);
    }

    public Movie update(Integer id, Movie incoming) {
        validateAgeLimit(incoming.getAgeLimit());
        var existing = byId(id);
        existing.setTitle(incoming.getTitle());
        existing.setPosterUrl(incoming.getPosterUrl());
        existing.setCategory(incoming.getCategory());
        existing.setAgeLimit(incoming.getAgeLimit());
        existing.setDurationMinutes(incoming.getDurationMinutes());
        existing.setActors(incoming.getActors());
        existing.setDescription(incoming.getDescription());
        return movies.save(existing);
    }

    public void delete(Integer id) {
        long active = shows.countByMovieIdAndStatus(id, ShowStatus.scheduled);
        if (active > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Kan ikke slette – aktive forestillinger findes");
        }
        movies.deleteById(id);
    }

    private void validateAgeLimit(Integer age) {
        if (age == null || !ALLOWED_AGE_LIMITS.contains(age)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Aldersgrænse skal være en af: 0, 7, 11, 15, 18");
        }
    }
}
