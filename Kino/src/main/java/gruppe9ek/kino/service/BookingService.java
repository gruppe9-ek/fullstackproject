package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Booking;
import gruppe9ek.kino.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;

    public BookingService(BookingRepository bookings) {
        this.bookings = bookings;
    }

    public List<Booking> all() {
        return bookings.findAll();
    }

    public Booking byId(Integer id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking blev ikke fundet"));
    }

    public Booking create(Booking b) {
        b.setBookingId(null);
        return bookings.save(b);
    }

    public void delete(Integer id) {
        bookings.deleteById(id);
    }
}
