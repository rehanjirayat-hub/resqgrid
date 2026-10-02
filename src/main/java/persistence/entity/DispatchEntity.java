package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA mapping of the {@code dispatches} table.
 *
 * This entity owns the foreign keys for the dispatch relationship, so the
 * incident, resource and response team sides are all mapped here. The response
 * team is optional, matching the nullable column in the schema.
 */
@Entity
@Table(name = "dispatches")
public class DispatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private ResourceEntity resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_team_id")
    private ResponseTeamEntity responseTeam;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected DispatchEntity() {
    }

    public DispatchEntity(IncidentEntity incident,
                          ResourceEntity resource,
                          ResponseTeamEntity responseTeam,
                          String status,
                          LocalDateTime createdAt) {

        this.incident = incident;
        this.resource = resource;
        this.responseTeam = responseTeam;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public IncidentEntity getIncident() {
        return incident;
    }

    public ResourceEntity getResource() {
        return resource;
    }

    public ResponseTeamEntity getResponseTeam() {
        return responseTeam;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}