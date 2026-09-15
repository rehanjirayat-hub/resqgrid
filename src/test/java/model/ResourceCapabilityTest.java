package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceCapabilityTest {


    @Test
    void shouldContainAllDocumentedCapabilities() {
        assertEquals(3, ResourceCapability.values().length);

        assertTrue(java.util.Arrays.asList(ResourceCapability.values())
                .contains(ResourceCapability.MEDICAL_RESPONSE));

        assertTrue(java.util.Arrays.asList(ResourceCapability.values())
                .contains(ResourceCapability.FIRE_RESPONSE));

        assertTrue(java.util.Arrays.asList(ResourceCapability.values())
                .contains(ResourceCapability.RESCUE_OPERATION));
    }
}
