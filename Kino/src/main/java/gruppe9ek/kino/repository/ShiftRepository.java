package gruppe9ek.kino.repository;

import gruppe9ek.kino.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;


public interface ShiftRepository extends JpaRepository<Shift, Integer> {
    boolean existsByDateAndStaff_StaffId(LocalDate date, Integer staffId);

    List<Shift> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

}

