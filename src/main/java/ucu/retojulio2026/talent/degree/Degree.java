package ucu.retojulio2026.talent.degree;

import jakarta.persistence.*;
import lombok.*;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

//Lombok para no tener que generar los Getters, Setters y Constructores básicos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity    // Le dice al ORM (Hibernate) que es una Entidad de la base de datos
@Table(name = "degree")
public class Degree {

    @Id // Indica que es una Primary Key en la base de datos
    @Column(name = "degree_id", length = 12, updatable = false, nullable = false)
    //Indica que es una columna a mapear en la base de datos y sus restricciones.
    private String degreeId;

    //Id del area a la que pertenece la carrera.
    //Se mantiene como String hasta que la entidad Area esté disponible.
    @Column(name = "area_id", length = 12, nullable = false)
    private String areaId;

    @Column(nullable = false, length = 100)
    private String name;

    //Indica si la carrera pertenece a la UCU.
    @Column(name = "is_ucu", nullable = false)
    private Boolean isUcu;

    //Metodo para generar IDs unicos, que no sean tan largos como UUIDs. Usar en todos los IDS!
    @PrePersist
    protected void assignId() {
        if (this.degreeId == null) {
            this.degreeId = NanoIdGenerator.generate();
        }
    }
}