package ucu.retojulio2026.talent.Vacancy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final IVacancyService vacancyService;

    public VacancyController(IVacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
    public List<Vacancy> getAllVacancies() {
        return vacancyService.getAllVacancies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(vacancyService.getVacancyById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(@RequestBody Vacancy vacancy) {

        Vacancy created = vacancyService.saveVacancy(vacancy);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long id,
            @RequestBody Vacancy vacancy) {

        try {
            return ResponseEntity.ok(vacancyService.updateVacancy(id, vacancy));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {

        try {
            vacancyService.deleteVacancy(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}