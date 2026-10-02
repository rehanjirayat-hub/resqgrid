package persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA mapping of the {@code resources} table.
 *
 * Capabilities are many-to-many through the {@code resource_capabilities}
 * junction table. This entity owns that relationship because it is the side
 * that populates the join table.
 */
@Entity
@Table(name = "resources")
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "status", nullable = false)
    private String status;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false,
            cascade = CascadeType.PERSIST
    )
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "resource_capabilities",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private List<CapabilityEntity> capabilities = new ArrayList<>();

    @OneToMany(mappedBy = "resource")
    private List<DispatchEntity> dispatches = new ArrayList<>();

    protected ResourceEntity() {
    }

    public ResourceEntity(String type, String status, LocationEntity location) {
        this.type = type;
        this.status = status;
        this.location = location;
    }

    public void addCapability(CapabilityEntity capability) {
        capabilities.add(capability);
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public List<CapabilityEntity> getCapabilities() {
        return capabilities;
    }

    public List<DispatchEntity> getDispatches() {
        return dispatches;
    }
}