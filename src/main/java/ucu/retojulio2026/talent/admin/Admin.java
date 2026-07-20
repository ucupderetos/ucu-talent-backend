package ucu.retojulio2026.talent.admin;

import jakarta.persistence.*;
import lombok.*;

//Lombok para no tener que generar los Getters, Setters y Constructores básicos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "admin")

public class Admin {

    @Id
    @Column(name = "admin_id", length = 12, updatable = false, nullable = false)
    private String adminId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

}
