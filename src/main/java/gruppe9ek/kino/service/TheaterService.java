package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Theater;
import gruppe9ek.kino.repository.TheaterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TheaterService {
    private final TheaterRepository repo;

    public TheaterService(TheaterRepository repo) {
        this.repo = repo;
    }

    public List<Theater> all() {
        return repo.findAll();
    }

    public Theater byId(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Theater not found"));
    }

    public Theater create(Theater theater) {
        theater.setTheaterId(null);
        try {
            return repo.save(theater);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Theater name must be unique or invalid data");
        }
    }

    public Theater update(Integer id, Theater incoming) {
        var existing = byId(id);
        existing.setTheaterName(incoming.getTheaterName());
        existing.setTotalRows(incoming.getTotalRows());
        existing.setSeatsPerRow(incoming.getSeatsPerRow());

        try {
            return repo.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Theater name must be unique or invalid data");
        }
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}
