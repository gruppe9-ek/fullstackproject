package gruppe9ek.kino.web;

import gruppe9ek.kino.entity.Shift;
import gruppe9ek.kino.entity.StaffRole;
import gruppe9ek.kino.service.RosterService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/roster")
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    /**
     * 1️⃣ Opret en vagt
     * Eksempel:
     * POST http://localhost:8080/api/roster/assign?date=2025-10-08&staffId=1&role=KIOSK
     */
    @PostMapping("/assign")
    public ResponseEntity<Shift> assignShift(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer staffId,
            @RequestParam StaffRole role) {

        Shift created = rosterService.assign(date, staffId, role);
        return ResponseEntity.ok(created);
    }

    /**
     * 2️⃣ Hent alle vagter i en uge (mandag–søndag)
     * Eksempel:
     * GET http://localhost:8080/api/roster?monday=2025-10-06
     */
    @GetMapping
    public List<Shift> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monday) {
        return rosterService.getWeek(monday);
    }

    /**
     * 3️⃣ Kopiér vagter fra én uge til en anden
     * Eksempel:
     * POST http://localhost:8080/api/roster/copy?fromMonday=2025-09-29&toMonday=2025-10-06
     */
    @PostMapping("/copy")
    public ResponseEntity<RosterService.CopyResult> copyWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromMonday,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toMonday) {

        RosterService.CopyResult result = rosterService.copyWeek(fromMonday, toMonday);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/shift/{id}/role")
    public ResponseEntity<Shift> changeRole(
            @PathVariable Integer id,
            @RequestParam StaffRole role) {
        Shift updated = rosterService.changeRole(id, role);
        return ResponseEntity.ok(updated);
    }

}
