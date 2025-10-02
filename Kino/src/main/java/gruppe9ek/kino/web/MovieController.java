package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Movie;
import gruppe9ek.kino.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService service;
    public MovieController(MovieService service) { this.service = service; }

    @GetMapping public List<Movie> all() { return service.all(); }

    @GetMapping("/{id}") public Movie one(@PathVariable Integer id) { return service.byId(id); }

    @PostMapping
    public ResponseEntity<Movie> create(@RequestBody @Valid Movie m) {
        var created = service.create(m);
        return ResponseEntity.created(URI.create("/api/movies/" + created.getMovieId())).body(created);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable Integer id, @RequestBody @Valid Movie m) {
        return service.update(id, m);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
