
package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Show;
import gruppe9ek.kino.entity.ShowStatus;
import gruppe9ek.kino.repository.ShowRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShowService {
    private final ShowRepository repo;

    public ShowService(ShowRepository repo) { this.repo = repo; }

    public List<Show> all() { return repo.findAll(); }

    public Show byId(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found"));
    }

    public Show create(Show s) {
        s.setShowId(null);
        try {
            return repo.save(s);
        } catch (DataIntegrityViolationException e) {
            // Unique theater/time or DB CHECK violations
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Theater/time conflict or invalid data");
        }
    }

    public Show update(Integer id, Show incoming) {
        var existing = byId(id);
        existing.setMovieId(incoming.getMovieId());
        existing.setTheaterId(incoming.getTheaterId());
        existing.setShowDatetime(incoming.getShowDatetime());
        existing.setPrice(incoming.getPrice());
        existing.setOperatorId(incoming.getOperatorId());
        existing.setInspectorId(incoming.getInspectorId());
        if (incoming.getStatus() != null) existing.setStatus(incoming.getStatus());

        try {
            return repo.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Theater/time conflict or invalid data");
        }
    }

    public Show cancel(Integer id) {
        var s = byId(id);
        s.setStatus(ShowStatus.cancelled);
        return repo.save(s);
    }

    public void delete(Integer id) { repo.deleteById(id); }
}
