package ucu.retojulio2026.talent.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

import java.time.LocalDate;

//Lombok para no tener que generar los Getters, Setters y Constructores básicos.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity    // Le dice al ORM (Hibernate) que es una Entidad de la base de datos
@Table(name = "\"user\"") // "user" es palabra reservada en Postgres, por eso las comillas dobles
public class User {

    @Id // Indica que es una Primary Key en la base de datos
    @Column(name = "user_id", length = 12, updatable = false, nullable = false)
    //Indica que es una calumna a mapear en la base de datos y sus restricciones.
    private String userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String surname;

    @Column(unique = true, nullable = false)
    private String email;

    @ToString.Exclude // nunca loguear el hash de la contraseña
    @Column(name = "password_hash", length = 60, nullable = false) // BCrypt produce siempre 60 caracteres (largo fijo)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // Tipo y numero de documento. Nullable (ej: los usuarios EMPRESA pueden no tenerlo).
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", length = 20)
    private String documentNumber;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    // Hibernate setea la fecha de alta automaticamente en el insert.
    @CreationTimestamp
    @Column(name = "registered_at", updatable = false, nullable = false)
    private LocalDate registeredAt;


    //Metodo para generar IDs unicos, que no sean tan largos como UUIDs. Usar en todos los IDS!
    @PrePersist
    protected void assignId() {
        if (this.userId == null) {
            this.userId = NanoIdGenerator.generate();
        }
    }

}
