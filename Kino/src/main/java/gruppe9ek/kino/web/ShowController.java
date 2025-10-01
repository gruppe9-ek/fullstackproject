// src/main/java/gruppe9ek/kino/web/ShowController.java
package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Show;
import gruppe9ek.kino.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin
public class ShowController {
    private final ShowService service;
    public ShowController(ShowService service) { this.service = service; }

    @GetMapping public List<Show> all() { return service.all(); }
    @GetMapping("/{id}") public Show one(@PathVariable Integer id) { return service.byId(id); }

    @PostMapping
    public ResponseEntity<Show> create(@RequestBody @Valid Show s) {
        var created = service.create(s);
        return ResponseEntity.created(URI.create("/api/shows/" + created.getShowId())).body(created);
    }

    @PutMapping("/{id}")
    public Show update(@PathVariable Integer id, @RequestBody @Valid Show s) {
        return service.update(id, s);
    }

    @PostMapping("/{id}/cancel")
    public Show cancel(@PathVariable Integer id) { return service.cancel(id); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
