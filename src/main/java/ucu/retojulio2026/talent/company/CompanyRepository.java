package ucu.retojulio2026.talent.company;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ucu.retojulio2026.talent.company.dto.PendingCompanyRow;
import ucu.retojulio2026.talent.user.AccountStatus;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    @Query("""
            SELECT COUNT(c)
            FROM Company c
            JOIN User u ON u.userId = c.companyId
            WHERE u.status = :status
            """)
    long countByAccountStatus(@Param("status") AccountStatus status);

    @Query("""
            SELECT new ucu.retojulio2026.talent.company.dto.PendingCompanyRow(
                c.companyId,
                c.name,
                c.industry,
                u.registeredAt
            )
            FROM Company c
            JOIN User u ON u.userId = c.companyId
            WHERE u.status = ucu.retojulio2026.talent.user.AccountStatus.PENDIENTE
            ORDER BY u.registeredAt DESC
            """)
    List<PendingCompanyRow> findPendingForDashboard(Pageable pageable);
}
