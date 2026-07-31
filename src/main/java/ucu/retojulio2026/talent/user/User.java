package ucu.retojulio2026.talent.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ucu.retojulio2026.talent.common.NanoIdGenerator;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @Column(name = "user_id", length = 12, updatable = false, nullable = false)

    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    @ToString.Exclude
    @Column(name = "password_hash", length = 60, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "profile_image", length = 255, nullable = true)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false, nullable = false)
    private LocalDate registeredAt;

    @PrePersist
    protected void assignId() {
        if (this.userId == null) {
            this.userId = NanoIdGenerator.generate();
        }
    }

}
