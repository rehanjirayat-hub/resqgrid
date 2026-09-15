package model;

import java.util.ArrayList;
import java.util.List;

public class ResponseTeam {

    private long id;
    private ResponseTeamStatus status;
    private List<ResourceCapability> capabilities;
    private List<Dispatch> currentAssignments;

    public ResponseTeam(long id) {
        this.id = id;
        this.status = ResponseTeamStatus.AVAILABLE;
        this.capabilities = new ArrayList<>();
        this.currentAssignments = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public ResponseTeamStatus getStatus() {
        return status;
    }

    public List<ResourceCapability> getCapabilities() {
        return capabilities;
    }

    public List<Dispatch> getCurrentAssignments() {
        return currentAssignments;
    }

    public void addCapability(ResourceCapability capability) {
        capabilities.add(capability);
    }
}