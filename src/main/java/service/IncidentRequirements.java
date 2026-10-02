package service;

import model.ResourceCapability;
import model.ResourceType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * The response requirements determined for an incident before resource
 * selection begins.
 *
 * The engine uses these requirements as given and never invents additional
 * requirements while selecting a resource.
 */
public final class IncidentRequirements {

    private final ResourceType requiredResourceType;
    private final Set<ResourceCapability> requiredCapabilities;
    private final boolean responseTeamRequired;

    private IncidentRequirements(ResourceType requiredResourceType,
                                 Set<ResourceCapability> requiredCapabilities,
                                 boolean responseTeamRequired) {

        this.requiredResourceType = requiredResourceType;
        this.requiredCapabilities = requiredCapabilities;
        this.responseTeamRequired = responseTeamRequired;
    }

    public static IncidentRequirements of(ResourceType requiredResourceType) {
        return new IncidentRequirements(
                requiredResourceType,
                EnumSet.noneOf(ResourceCapability.class),
                false
        );
    }

    public IncidentRequirements requiring(ResourceCapability capability) {
        Objects.requireNonNull(capability, "capability must not be null");

        Set<ResourceCapability> updated =
                EnumSet.noneOf(ResourceCapability.class);

        updated.addAll(requiredCapabilities);
        updated.add(capability);

        return new IncidentRequirements(
                requiredResourceType,
                updated,
                responseTeamRequired
        );
    }

    public IncidentRequirements withResponseTeam() {
        return new IncidentRequirements(
                requiredResourceType,
                requiredCapabilities,
                true
        );
    }

    public boolean hasResourceTypeRequirement() {
        return requiredResourceType != null;
    }

    public ResourceType getRequiredResourceType() {
        return requiredResourceType;
    }

    public Set<ResourceCapability> getRequiredCapabilities() {
        return Collections.unmodifiableSet(requiredCapabilities);
    }

    public boolean isResponseTeamRequired() {
        return responseTeamRequired;
    }
}