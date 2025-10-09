package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
}
