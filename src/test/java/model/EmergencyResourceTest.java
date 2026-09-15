package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmergencyResourceTest {

    @Test
    void newResourceShouldStartAsAvailable(){
        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                new Location(12,15)
        );

        assertEquals(ResourceStatus.AVAILABLE,resource.getStatus());
        assertEquals(ResourceType.AMBULANCE, resource.getType());
    }

    @Test
    void resourceShouldStoreBasicInformation(){
        Location location = new Location(12, 15);
        EmergencyResource resource = new EmergencyResource(
                1,
                ResourceType.AMBULANCE,
                location
        );

        assertEquals(1,resource.getId());
        assertEquals(ResourceStatus.AVAILABLE,resource.getStatus());
        assertEquals(12,resource.getLocation().getLatitude());
        assertEquals(15, resource.getLocation().getLongitude());
    }
}
