package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Booking;
import gruppe9ek.kino.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getAll() {
        return bookingService.all();
    }

    @GetMapping("/{id}")
    public Booking getById(@PathVariable Integer id) {
        return bookingService.byId(id);
    }

    @PostMapping
    public Booking create(@RequestBody Booking booking) {
        return bookingService.create(booking);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        bookingService.delete(id);
    }
}
