package repository;

import model.Incident;
import model.Location;
import model.Severity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentRepositoryTest {

    @Test
    void saveShouldInsertIncidentAndReturnGeneratedId() {

        Location location = new Location(12.9716, 77.5946);

        Incident incident = new Incident(
                "Medical emergency",
                Severity.CRITICAL,
                location
        );

        LocationRepository locationRepository = new LocationRepository();
        IncidentRepository incidentRepository =
                new IncidentRepository(locationRepository);

        long id = incidentRepository.save(incident);

        assertTrue(id > 0);
    }
}