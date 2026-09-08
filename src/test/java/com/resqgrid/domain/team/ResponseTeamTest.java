package com.resqgrid.domain.team;

import com.resqgrid.domain.resource.Capability;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTeamTest {

    private ResponseTeam createTeam() {
        return new ResponseTeam(
                1L,
                "Medical Team Alpha",
                Set.of(
                        Capability.MEDICAL_RESPONSE,
                        Capability.RESCUE
                )
        );
    }

    @Test
    void teamShouldStartAsAvailable() {
        ResponseTeam team = createTeam();

        assertEquals(TeamStatus.AVAILABLE, team.getStatus());
    }

    @Test
    void teamShouldStoreBasicDetails() {
        ResponseTeam team = createTeam();

        assertEquals(1L, team.getId());
        assertEquals("Medical Team Alpha", team.getName());
    }

    @Test
    void teamShouldStoreCapabilities() {
        ResponseTeam team = createTeam();

        assertTrue(team.getCapabilities().contains(
                Capability.MEDICAL_RESPONSE
        ));

        assertTrue(team.getCapabilities().contains(
                Capability.RESCUE
        ));
    }

    @Test
    void teamShouldChangeFromAvailableToDeployed() {
        ResponseTeam team = createTeam();

        team.changeStatus(TeamStatus.DEPLOYED);

        assertEquals(TeamStatus.DEPLOYED, team.getStatus());
    }

    @Test
    void teamShouldReturnToAvailableFromDeployed() {
        ResponseTeam team = createTeam();

        team.changeStatus(TeamStatus.DEPLOYED);
        team.changeStatus(TeamStatus.AVAILABLE);

        assertEquals(TeamStatus.AVAILABLE, team.getStatus());
    }

    @Test
    void teamShouldChangeFromAvailableToOffline() {
        ResponseTeam team = createTeam();

        team.changeStatus(TeamStatus.OFFLINE);

        assertEquals(TeamStatus.OFFLINE, team.getStatus());
    }

    @Test
    void teamShouldReturnToAvailableFromOffline() {
        ResponseTeam team = createTeam();

        team.changeStatus(TeamStatus.OFFLINE);
        team.changeStatus(TeamStatus.AVAILABLE);

        assertEquals(TeamStatus.AVAILABLE, team.getStatus());
    }

    @Test
    void teamShouldRejectInvalidStatusTransition() {
        ResponseTeam team = createTeam();

        team.changeStatus(TeamStatus.OFFLINE);

        assertThrows(
                IllegalStateException.class,
                () -> team.changeStatus(TeamStatus.DEPLOYED)
        );
    }

    @Test
    void teamShouldRejectNullStatus() {
        ResponseTeam team = createTeam();

        assertThrows(
                IllegalArgumentException.class,
                () -> team.changeStatus(null)
        );
    }

    @Test
    void teamShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTeam(
                        0L,
                        "Medical Team Alpha",
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void teamShouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTeam(
                        1L,
                        "   ",
                        Set.of(Capability.MEDICAL_RESPONSE)
                )
        );
    }

    @Test
    void teamShouldRejectNullCapabilities() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTeam(
                        1L,
                        "Medical Team Alpha",
                        null
                )
        );
    }

    @Test
    void teamShouldRejectEmptyCapabilities() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTeam(
                        1L,
                        "Medical Team Alpha",
                        Set.of()
                )
        );
    }

    @Test
    void teamShouldNotExposeMutableCapabilities() {
        ResponseTeam team = createTeam();

        Set<Capability> capabilities = team.getCapabilities();

        assertThrows(
                UnsupportedOperationException.class,
                capabilities::clear
        );

        assertEquals(2, team.getCapabilities().size());
    }
}