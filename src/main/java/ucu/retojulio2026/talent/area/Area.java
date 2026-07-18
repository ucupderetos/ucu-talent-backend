package ucu.retojulio2026.talent.area;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "area")
public class Area {
    @Id
    @Column(name = "area_id", length = 12, updatable = false, nullable = false)
    private String areaId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parent_area_id", length = 12)
    private String parentAreaId;

    @PrePersist
    protected void assignId() {
        if (this.areaId == null) {
            this.areaId = NanoIdGenerator.generate();
        }
    }
}