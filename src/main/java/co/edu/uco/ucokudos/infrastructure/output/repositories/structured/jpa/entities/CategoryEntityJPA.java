package co.edu.uco.ucokudos.infrastructure.output.repositories.structured.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryEntityJPA {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active;

    protected CategoryEntityJPA() {
        // Requerido por JPA
    }

    public CategoryEntityJPA(UUID id, String name, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
