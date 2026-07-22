package ucu.retojulio2026.talent.vacancy.filter.strategy;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ucu.retojulio2026.talent.vacancy.Vacancy;
import ucu.retojulio2026.talent.vacancy.filter.VacancyFilterStrategy;
import ucu.retojulio2026.talent.vacancy.dto.SearchCriteriaVacancyRequest;

/**
 * Vacancy.contractType es un String, no un enum (a diferencia
 * de por ej: Department). Este filtro hace match exacto case-insensitive,
 * asi que si en la base hay valores con espacios distintos o sinonimos
 * ("Tiempo completo" vs "tiempo_completo"), no van a matchear. Si esto
 * genera problemas en la practica, la solucion de fondo es convertir
 * contractType a un enum.
 */
@Component
public class ContractTypeVacancyFilterStrategy implements VacancyFilterStrategy {

    @Override
    public Optional<Specification<Vacancy>> resolve(SearchCriteriaVacancyRequest criteria) {
        if (criteria.contractType() == null || criteria.contractType().isBlank()) {
            return Optional.empty();
        }
        String valor = criteria.contractType().trim();
        return Optional.of((root, query, cb) ->
                cb.equal(cb.lower(root.get("contractType")), valor.toLowerCase()));
    }
}