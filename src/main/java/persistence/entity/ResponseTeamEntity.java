package persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping of the {@code response_teams} table.
 *
 * Team capabilities use their own junction table, separate from the resource
 * capability table, because a team and a resource are different owners.
 */
@Entity
@Table(name = "response_teams")
public class ResponseTeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_capabilities",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private List<CapabilityEntity> capabilities = new ArrayList<>();

    @OneToMany(mappedBy = "responseTeam")
    private List<DispatchEntity> dispatches = new ArrayList<>();

    protected ResponseTeamEntity() {
    }

    public ResponseTeamEntity(String status) {
        this.status = status;
    }

    public void addCapability(CapabilityEntity capability) {
        capabilities.add(capability);
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public List<CapabilityEntity> getCapabilities() {
        return capabilities;
    }
}