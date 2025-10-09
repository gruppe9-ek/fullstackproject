package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {}
