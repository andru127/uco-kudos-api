package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntityJPA {

    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserEntityJPA() {
        // Requerido por JPA
    }

    public UserEntityJPA(UUID id, String fullName, String email, String phone, String role, boolean active,
                         String passwordHash, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public void changeContact(String newEmail, String newPhone) {
        this.email = newEmail;
        this.phone = newPhone;
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
}
