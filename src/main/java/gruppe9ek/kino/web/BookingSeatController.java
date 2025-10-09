package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.BookingSeat;
import gruppe9ek.kino.service.BookingSeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-seats")
public class BookingSeatController {
    private final BookingSeatService bookingSeatService;

    public BookingSeatController(BookingSeatService bookingSeatService) {
        this.bookingSeatService = bookingSeatService;
    }

    @GetMapping
    public List<BookingSeat> getAll() {
        return bookingSeatService.all();
    }

    @GetMapping("/{id}")
    public BookingSeat getById(@PathVariable Integer id) {
        return bookingSeatService.byId(id);
    }

    @PostMapping
    public BookingSeat create(@RequestBody BookingSeat bookingSeat) {
        return bookingSeatService.create(bookingSeat);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        bookingSeatService.delete(id);
    }
}
