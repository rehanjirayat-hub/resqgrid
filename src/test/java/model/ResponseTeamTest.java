package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseTeamTest {

    @Test
    void newTeamShouldStartAsAvailable(){
        ResponseTeam responseTeam = new ResponseTeam(
                1
        );

        assertEquals(ResponseTeamStatus.AVAILABLE, responseTeam.getStatus());
    }

    @Test
    void newTeamShouldHaveNoCapabilities() {
        ResponseTeam responseTeam = new ResponseTeam(1);

        assertTrue(responseTeam.getCapabilities().isEmpty());
    }

    @Test
    void newTeamShouldHaveNoCurrentAssignments(){
        ResponseTeam responseTeam = new ResponseTeam(1);

        assertTrue(responseTeam.getCurrentAssignments().isEmpty());
    }

    @Test
    void teamShouldStoreItsId(){
        ResponseTeam responseTeam = new ResponseTeam(1);

        assertEquals(1,responseTeam.getId());
    }

    @Test
    void teamShouldAddCapability() {
        ResponseTeam responseTeam = new ResponseTeam(1);

        responseTeam.addCapability(ResourceCapability.MEDICAL_RESPONSE);

        assertTrue(responseTeam.getCapabilities()
                .contains(ResourceCapability.MEDICAL_RESPONSE));
    }

    @Test
    void teamShouldStoreCapabilities() {
        ResponseTeam responseTeam = new ResponseTeam(1);

        responseTeam.addCapability(ResourceCapability.MEDICAL_RESPONSE);
        responseTeam.addCapability(ResourceCapability.FIRE_RESPONSE);

        assertEquals(2, responseTeam.getCapabilities().size());
        assertTrue(responseTeam.getCapabilities()
                .contains(ResourceCapability.MEDICAL_RESPONSE));
        assertTrue(responseTeam.getCapabilities()
                .contains(ResourceCapability.FIRE_RESPONSE));
    }

    @Test
    void teamShouldStoreCurrentAssignment(){


        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                new Location(12.9716, 77.5946)
        );

        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12.9716, 77.5946)
        );

        ResponseTeam responseTeam = new ResponseTeam(1);

        Dispatch dispatch = new Dispatch(
                1,
                incident,
                resource,
                responseTeam
        );

        responseTeam.getCurrentAssignments().add(dispatch);

        assertTrue(responseTeam.getCurrentAssignments().contains(dispatch));



    }
}
