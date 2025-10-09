package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Theater;
import gruppe9ek.kino.service.TheaterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {
    private final TheaterService service;

    public TheaterController(TheaterService service) {
        this.service = service;
    }

    @GetMapping
    public List<Theater> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Theater one(@PathVariable Integer id) {
        return service.byId(id);
    }

    @PostMapping
    public ResponseEntity<Theater> create(@RequestBody @Valid Theater theater) {
        var created = service.create(theater);
        return ResponseEntity.created(URI.create("/api/theaters/" + created.getTheaterId())).body(created);
    }

    @PutMapping("/{id}")
    public Theater update(@PathVariable Integer id, @RequestBody @Valid Theater theater) {
        return service.update(id, theater);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
