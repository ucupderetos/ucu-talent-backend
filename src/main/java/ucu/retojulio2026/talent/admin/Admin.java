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
// Datos personales del Admin. Existe por la auditoria de moderacion: es de donde sale
// el nombre humano para mostrar en un "Aprobado/Rechazado por X".
// Ver docs/ADR/0005-separacion-user-perfiles.md
public class Admin {

    // PK compartida con el User dueño: siempre igual a userId. Ademas de PK es FK a
    // "user"(user_id) con ON DELETE CASCADE (V21), asi que el 1-a-1 y el borrado en
    // cascada los garantiza Postgres. No hay @PrePersist: el id nunca se genera aca,
    // lo asigna AdminMapper a partir del userId.
    @Id
    @Column(name = "admin_id", length = 12, updatable = false, nullable = false)
    private String adminId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

}
