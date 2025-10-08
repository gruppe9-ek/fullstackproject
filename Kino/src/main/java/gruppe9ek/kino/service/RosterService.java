package gruppe9ek.kino.service;

import gruppe9ek.kino.entity.Shift;
import gruppe9ek.kino.entity.Staff;
import gruppe9ek.kino.entity.StaffRole;
import gruppe9ek.kino.repository.ShiftRepository;
import gruppe9ek.kino.repository.StaffRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RosterService {

    private final ShiftRepository shiftRepo;
    private final StaffRepository staffRepo;

    public RosterService(ShiftRepository shiftRepo, StaffRepository staffRepo) {
        this.shiftRepo = shiftRepo;
        this.staffRepo = staffRepo;
    }

    /**
     * Opret en vagt til en medarbejder.
     * 404 hvis medarbejder-id ikke findes.
     * 409 hvis medarbejderen allerede har en vagt på den dato.
     */
    public Shift assign(LocalDate date, Integer staffId, StaffRole role) {
        // 404 hvis medarbejderen ikke findes
        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ugyldigt staff-id"));

        // 409 hvis der allerede findes en vagt samme dato for samme medarbejder
        if (shiftRepo.existsByDateAndStaff_StaffId(date, staffId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medarbejder har allerede en vagt den dag");
        }

        try {
            Shift shift = new Shift();
            shift.setDate(date);
            shift.setRole(role);
            shift.setStaff(staff);
            return shiftRepo.save(shift);
        } catch (DataIntegrityViolationException e) {
            // ekstra sikkerhedsnet hvis DB-unikhed udløses
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Medarbejder har allerede en vagt den dag");
        }
    }

    /** Hent alle vagter i en uge (mandag–søndag) */
    public List<Shift> getWeek(LocalDate monday) {
        LocalDate start = monday;
        LocalDate end = monday.plusDays(6);
        return shiftRepo.findByDateBetweenOrderByDateAsc(start, end);
    }

    /**
     * Kopiér alle vagter fra én uge til en anden uge.
     * Konflikter (dobbeltbook pr. dato/medarbejder) springes over.
     */
    public CopyResult copyWeek(LocalDate fromMonday, LocalDate toMonday) {
        LocalDate fromStart = fromMonday;
        LocalDate fromEnd = fromMonday.plusDays(6);
        List<Shift> source = shiftRepo.findByDateBetweenOrderByDateAsc(fromStart, fromEnd);

        int created = 0;
        int skipped = 0;

        for (Shift s : source) {
            long delta = ChronoUnit.DAYS.between(fromMonday, s.getDate());
            LocalDate targetDate = toMonday.plusDays(delta);

            // Skip hvis medarbejderen allerede har en vagt på targetDate
            Integer staffId = s.getStaff().getStaffId();
            if (shiftRepo.existsByDateAndStaff_StaffId(targetDate, staffId)) {
                skipped++;
                continue;
            }

            try {
                Shift copy = new Shift();
                copy.setDate(targetDate);
                copy.setRole(s.getRole());
                copy.setStaff(s.getStaff());
                shiftRepo.save(copy);
                created++;
            } catch (DataIntegrityViolationException e) {
                // Hvis to kilder rammer samme target, eller race condition → skip
                skipped++;
            }
        }
        return new CopyResult(created, skipped);
    }

    public Shift changeRole(Integer shiftId, StaffRole role) {
        // Find eksisterende vagt eller 404
        Shift shift = shiftRepo.findById(shiftId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift ikke fundet"));

        // Opdater rollen og gem
        shift.setRole(role);
        return shiftRepo.save(shift);
    }


    /** Lille returtype til /copy endpointet */
    public static class CopyResult {
        public final int created;
        public final int skipped;

        public CopyResult(int created, int skipped) {
            this.created = created;
            this.skipped = skipped;
        }
    }
}
