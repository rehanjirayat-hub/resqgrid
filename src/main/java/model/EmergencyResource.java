package model;

public class EmergencyResource {

    private long id;
    private ResourceType type;
    private ResourceStatus status;
    private Location location;

    public long getId() {
        return id;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public EmergencyResource(long id, ResourceType type, Location location) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.status = ResourceStatus.AVAILABLE;
    }
}