package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Seat;
import gruppe9ek.kino.service.SeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public List<Seat> getAll() {
        return seatService.all();
    }

    @GetMapping("/{id}")
    public Seat getById(@PathVariable Integer id) {
        return seatService.byId(id);
    }

    @PostMapping
    public Seat create(@RequestBody Seat seat) {
        return seatService.create(seat);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        seatService.delete(id);
    }
}
