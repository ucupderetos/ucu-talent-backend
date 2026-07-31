package ucu.retojulio2026.talent.degree;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "degree")
public class Degree {

    @Id
    @Column(name = "degree_id", length = 12, updatable = false, nullable = false)

    private String degreeId;

    @Column(name = "area_id", length = 12, nullable = false)
    private String areaId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_ucu", nullable = false)
    private Boolean isUcu;

    @PrePersist
    protected void assignId() {
        if (this.degreeId == null) {
            this.degreeId = NanoIdGenerator.generate();
        }
    }
}
