package persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping of the {@code incidents} table.
 *
 * The location is required, so the association is non-optional. Dispatches are
 * mapped as the inverse side because the dispatch row holds the foreign key.
 */
@Entity
@Table(name = "incidents")
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false,
            cascade = CascadeType.PERSIST
    )
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "incident")
    private List<DispatchEntity> dispatches = new ArrayList<>();

    protected IncidentEntity() {
    }

    public IncidentEntity(String description,
                          String severity,
                          String status,
                          LocationEntity location,
                          LocalDateTime createdAt) {

        this.description = description;
        this.severity = severity;
        this.status = status;
        this.location = location;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public String getStatus() {
        return status;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<DispatchEntity> getDispatches() {
        return dispatches;
    }

    /**
     * Changes the stored status. This exists so tests and persistence code can
     * exercise dirty checking; lifecycle rules are enforced by the service
     * layer, not by the entity.
     */
    public void advanceTo(String status) {
        this.status = status;
    }
}