package com.resqgrid.service.dispatch;

import com.resqgrid.domain.dispatch.Dispatch;
import com.resqgrid.domain.incident.Incident;
import com.resqgrid.domain.incident.IncidentSeverity;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.incident.IncidentType;
import com.resqgrid.domain.location.Location;
import com.resqgrid.domain.resource.Capability;
import com.resqgrid.domain.resource.EmergencyResource;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.resource.ResourceType;
import com.resqgrid.domain.team.ResponseTeam;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceDispatchConcurrencyTest {

    @Test
    void onlyOneIncidentShouldReceiveTheSameResourceConcurrently()
            throws Exception {

        for (int attempt = 0; attempt < 100; attempt++) {

            ResourceDispatchService dispatchService =
                    new ResourceDispatchService();

            EmergencyResource ambulance = createAmbulance();

            Incident firstIncident = createIncident(
                    1L,
                    "Medical Emergency A"
            );

            Incident secondIncident = createIncident(
                    2L,
                    "Medical Emergency B"
            );

            ResponseTeam firstTeam = createTeam(
                    1L,
                    "Medical Team Alpha"
            );

            ResponseTeam secondTeam = createTeam(
                    2L,
                    "Medical Team Bravo"
            );

            CountDownLatch startLatch = new CountDownLatch(1);

            ExecutorService executorService =
                    Executors.newFixedThreadPool(2);

            try {
                Future<Dispatch> firstDispatch =
                        executorService.submit(() -> {
                            startLatch.await();

                            return dispatchService.dispatchIncident(
                                    firstIncident,
                                    List.of(ambulance),
                                    List.of(firstTeam)
                            );
                        });

                Future<Dispatch> secondDispatch =
                        executorService.submit(() -> {
                            startLatch.await();

                            return dispatchService.dispatchIncident(
                                    secondIncident,
                                    List.of(ambulance),
                                    List.of(secondTeam)
                            );
                        });

                startLatch.countDown();

                int successfulDispatches = 0;

                try {
                    firstDispatch.get();
                    successfulDispatches++;
                } catch (ExecutionException ignored) {
                    // Expected for the losing thread.
                }

                try {
                    secondDispatch.get();
                    successfulDispatches++;
                } catch (ExecutionException ignored) {
                    // Expected for the losing thread.
                }

                assertEquals(
                        1,
                        successfulDispatches,
                        "Only one incident may claim the same resource"
                );

                assertEquals(
                        ResourceStatus.BUSY,
                        ambulance.getStatus()
                );

            } finally {
                executorService.shutdown();
            }
        }
    }

    private Incident createIncident(
            long id,
            String title
    ) {
        Incident incident = new Incident(
                id,
                title,
                "Patient requires immediate assistance",
                IncidentType.MEDICAL,
                IncidentSeverity.HIGH,
                createLocation(),
                LocalDateTime.now()
        );

        incident.changeStatus(IncidentStatus.ASSESSED);

        return incident;
    }

    private EmergencyResource createAmbulance() {
        return new EmergencyResource(
                1L,
                "Ambulance 101",
                ResourceType.AMBULANCE,
                createLocation(),
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private ResponseTeam createTeam(
            long id,
            String name
    ) {
        return new ResponseTeam(
                id,
                name,
                Set.of(Capability.MEDICAL_RESPONSE)
        );
    }

    private Location createLocation() {
        return new Location(
                12.2958,
                76.6394,
                "Mysuru, Karnataka"
        );
    }
}
