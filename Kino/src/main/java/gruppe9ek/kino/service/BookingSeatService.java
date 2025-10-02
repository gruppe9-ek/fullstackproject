package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.BookingSeat;
import gruppe9ek.kino.repository.BookingSeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingSeatService {
    private final BookingSeatRepository bookingSeats;

    public BookingSeatService(BookingSeatRepository bookingSeats) {
        this.bookingSeats = bookingSeats;
    }

    public List<BookingSeat> all() {
        return bookingSeats.findAll();
    }

    public BookingSeat byId(Integer id) {
        return bookingSeats.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BookingSeat blev ikke fundet"));
    }

    public BookingSeat create(BookingSeat bs) {
        bs.setBookingSeatId(null);
        return bookingSeats.save(bs);
    }

    public void delete(Integer id) {
        bookingSeats.deleteById(id);
    }
}
