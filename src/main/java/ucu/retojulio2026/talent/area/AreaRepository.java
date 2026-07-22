package ucu.retojulio2026.talent.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, String> {
    List<Area> findByParentAreaId(String parentAreaId);
}