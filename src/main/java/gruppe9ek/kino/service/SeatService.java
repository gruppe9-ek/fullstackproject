package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Seat;
import gruppe9ek.kino.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seats;

    public SeatService(SeatRepository seats) {
        this.seats = seats;
    }

    public List<Seat> all() {
        return seats.findAll();
    }

    public Seat byId(Integer id) {
        return seats.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sædet ikke fundet"));
    }

    public Seat create(Seat s) {
        return seats.save(s);
    }

    public void delete(Integer id) {
        seats.deleteById(id);
    }
}
