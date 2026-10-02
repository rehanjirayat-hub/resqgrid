package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA mapping of the {@code capabilities} table.
 *
 * The capability catalogue is a controlled set of values, so the entity only
 * stores the name; meaning belongs to the domain enum.
 */
@Entity
@Table(name = "capabilities")
public class CapabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    protected CapabilityEntity() {
    }

    public CapabilityEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}