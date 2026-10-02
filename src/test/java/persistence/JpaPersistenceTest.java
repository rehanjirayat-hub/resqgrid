package persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.entity.CapabilityEntity;
import persistence.entity.DispatchEntity;
import persistence.entity.IncidentEntity;
import persistence.entity.LocationEntity;
import persistence.entity.ResourceEntity;
import persistence.entity.ResponseTeamEntity;
import support.TestDataCleaner;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JpaPersistenceTest {

    private static EntityManagerFactory factory;

    @BeforeAll
    static void createFactory() {

        factory = EntityManagerFactoryProvider.create();
    }

    @AfterAll
    static void closeFactory() {

        if (factory != null) {
            factory.close();
        }
    }

    @BeforeEach
    void resetDatabase() {

        // The cleaner also seeds the controlled capability catalogue.
        TestDataCleaner.clearAll();
    }

    private CapabilityEntity capability(EntityManager entityManager, String name) {

        return entityManager.createQuery(
                        "from CapabilityEntity where name = :name", CapabilityEntity.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    @Test
    void incidentShouldRoundTripThroughJpa() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            LocationEntity location = new LocationEntity(12.9716, 77.5946);

            IncidentEntity incident = new IncidentEntity(
                    "Medical emergency",
                    "CRITICAL",
                    "REPORTED",
                    location,
                    LocalDateTime.now()
            );

            entityManager.persist(incident);

            entityManager.getTransaction().commit();

            assertNotNull(incident.getId());

            entityManager.clear();

            IncidentEntity loaded =
                    entityManager.find(IncidentEntity.class, incident.getId());

            assertNotNull(loaded);
            assertEquals("Medical emergency", loaded.getDescription());
            assertEquals("CRITICAL", loaded.getSeverity());
            assertEquals("REPORTED", loaded.getStatus());

        } finally {
            entityManager.close();
        }
    }

    @Test
    void generatedIdentifiersShouldUseIdentityColumns() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            LocationEntity first = new LocationEntity(1.0, 1.0);
            LocationEntity second = new LocationEntity(2.0, 2.0);

            entityManager.persist(first);
            entityManager.persist(second);

            entityManager.getTransaction().commit();

            assertNotNull(first.getId());
            assertNotNull(second.getId());
            assertFalse(first.getId().equals(second.getId()));

        } finally {
            entityManager.close();
        }
    }

    @Test
    void manyToManyCapabilitiesShouldBeStoredInJunctionTable() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            ResourceEntity resource = new ResourceEntity(
                    "AMBULANCE",
                    "AVAILABLE",
                    new LocationEntity(12.9716, 77.5946)
            );

            resource.addCapability(
                    capability(entityManager, "MEDICAL_RESPONSE")
            );

            entityManager.persist(resource);
            entityManager.getTransaction().commit();

            entityManager.clear();

            ResourceEntity loaded =
                    entityManager.find(ResourceEntity.class, resource.getId());

            assertEquals(1, loaded.getCapabilities().size());
            assertEquals(
                    "MEDICAL_RESPONSE",
                    loaded.getCapabilities().get(0).getName()
            );

        } finally {
            entityManager.close();
        }
    }

    @Test
    void dispatchShouldLinkIncidentResourceAndOptionalTeam() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Structure fire",
                    "HIGH",
                    "REPORTED",
                    new LocationEntity(12.9716, 77.5946),
                    LocalDateTime.now()
            );

            ResourceEntity resource = new ResourceEntity(
                    "FIRE_UNIT",
                    "AVAILABLE",
                    new LocationEntity(13.0827, 80.2707)
            );

            entityManager.persist(incident);
            entityManager.persist(resource);

            entityManager.getTransaction().commit();

            entityManager.getTransaction().begin();

            DispatchEntity dispatch = new DispatchEntity(
                    incident,
                    resource,
                    null,
                    "CREATED",
                    LocalDateTime.now()
            );

            entityManager.persist(dispatch);
            entityManager.getTransaction().commit();

            entityManager.clear();

            DispatchEntity loaded =
                    entityManager.find(DispatchEntity.class, dispatch.getId());

            assertEquals(incident.getId(), loaded.getIncident().getId());
            assertEquals(resource.getId(), loaded.getResource().getId());
            assertNull(
                    loaded.getResponseTeam(),
                    "The response team is optional."
            );

        } finally {
            entityManager.close();
        }
    }

    @Test
    void dispatchShouldSupportAssociatedResponseTeam() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Rescue operation",
                    "HIGH",
                    "REPORTED",
                    new LocationEntity(19.0760, 72.8777),
                    LocalDateTime.now()
            );

            ResourceEntity resource = new ResourceEntity(
                    "RESCUE_TEAM",
                    "AVAILABLE",
                    new LocationEntity(19.0760, 72.8777)
            );

            ResponseTeamEntity team = new ResponseTeamEntity("AVAILABLE");

            team.addCapability(capability(entityManager, "RESCUE_OPERATION"));

            entityManager.persist(incident);
            entityManager.persist(resource);
            entityManager.persist(team);

            entityManager.getTransaction().commit();

            entityManager.getTransaction().begin();

            DispatchEntity dispatch = new DispatchEntity(
                    incident, resource, team, "CREATED", LocalDateTime.now()
            );

            entityManager.persist(dispatch);
            entityManager.getTransaction().commit();

            entityManager.clear();

            DispatchEntity loaded =
                    entityManager.find(DispatchEntity.class, dispatch.getId());

            assertNotNull(loaded.getResponseTeam());
            assertEquals(team.getId(), loaded.getResponseTeam().getId());

            entityManager.getTransaction().begin();

            ResponseTeamEntity loadedTeam = entityManager.find(
                    ResponseTeamEntity.class, team.getId());

            assertEquals(1, loadedTeam.getCapabilities().size());

            entityManager.getTransaction().commit();

        } finally {
            entityManager.close();
        }
    }

    @Test
    void inverseSideShouldBePopulatedWithoutSeparateUpdate() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Multi dispatch incident",
                    "MEDIUM",
                    "REPORTED",
                    new LocationEntity(28.6139, 77.2090),
                    LocalDateTime.now()
            );

            entityManager.persist(incident);

            ResourceEntity first = new ResourceEntity(
                    "AMBULANCE", "AVAILABLE", new LocationEntity(28.6, 77.2));

            ResourceEntity second = new ResourceEntity(
                    "AMBULANCE", "AVAILABLE", new LocationEntity(28.7, 77.3));

            entityManager.persist(first);
            entityManager.persist(second);

            entityManager.persist(new DispatchEntity(
                    incident, first, null, "CREATED", LocalDateTime.now()));

            entityManager.persist(new DispatchEntity(
                    incident, second, null, "CREATED", LocalDateTime.now()));

            entityManager.getTransaction().commit();

            entityManager.clear();

            IncidentEntity loaded =
                    entityManager.find(IncidentEntity.class, incident.getId());

            assertEquals(
                    2,
                    loaded.getDispatches().size(),
                    "mappedBy must resolve dispatches from the owning side."
            );

        } finally {
            entityManager.close();
        }
    }

    @Test
    void lazyAssociationsShouldLoadWithinTransaction() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Lazy load incident",
                    "LOW",
                    "REPORTED",
                    new LocationEntity(15.3173, 75.7139),
                    LocalDateTime.now()
            );

            entityManager.persist(incident);
            entityManager.getTransaction().commit();

            entityManager.clear();

            IncidentEntity loaded =
                    entityManager.find(IncidentEntity.class, incident.getId());

            // The location is lazy, so touching it must work while the
            // persistence context is still open.
            assertEquals(15.3173, loaded.getLocation().getLatitude(), 0.0001);

        } finally {
            entityManager.close();
        }
    }

    @Test
    void lazyAssociationShouldFailAfterEntityManagerIsClosed() {

        Long incidentId;

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Detached incident",
                    "LOW",
                    "REPORTED",
                    new LocationEntity(22.5726, 88.3639),
                    LocalDateTime.now()
            );

            entityManager.persist(incident);
            entityManager.getTransaction().commit();

            incidentId = incident.getId();

        } finally {
            entityManager.close();
        }

        EntityManager second = factory.createEntityManager();

        try {

            IncidentEntity detached =
                    second.find(IncidentEntity.class, incidentId);

            second.close();assertThrows(
                    Exception.class,
                    () -> detached.getLocation().getLatitude(),
                    "A lazy association must not load once the context is closed."
                );

        } finally {
            if (second.isOpen()) {
                second.close();
            }
        }
    }

    @Test
    void persistenceContextShouldReturnSameInstanceWithinTransaction() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "First level cache incident",
                    "LOW",
                    "REPORTED",
                    new LocationEntity(17.3850, 78.4867),
                    LocalDateTime.now()
            );

            entityManager.persist(incident);
            entityManager.getTransaction().commit();

            IncidentEntity first = entityManager.find(
                    IncidentEntity.class, incident.getId());

            IncidentEntity second = entityManager.find(
                    IncidentEntity.class, incident.getId());

            assertTrue(
                    first == second,
                    "The persistence context must return the same instance."
            );

        } finally {
            entityManager.close();
        }
    }

    @Test
    void dirtyCheckingShouldPersistFieldChange() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            IncidentEntity incident = new IncidentEntity(
                    "Dirty incident",
                    "LOW",
                    "REPORTED",
                    new LocationEntity(11.0168, 76.9558),
                    LocalDateTime.now()
            );

            entityManager.persist(incident);
            entityManager.getTransaction().commit();

            entityManager.getTransaction().begin();

            IncidentEntity managed =
                    entityManager.find(IncidentEntity.class, incident.getId());

            managed.advanceTo("ASSESSED");

            entityManager.getTransaction().commit();

            entityManager.clear();

            assertEquals(
                    "ASSESSED",
                    entityManager.find(IncidentEntity.class, incident.getId())
                               .getStatus()
            );

        } finally {
            entityManager.close();
        }
    }

    @Test
    void failedTransactionShouldRollbackEntirely() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            entityManager.getTransaction().begin();

            entityManager.persist(new IncidentEntity(
                    "Rolled back incident",
                    "LOW",
                    "REPORTED",
                    new LocationEntity(9.9312, 76.2673),
                    LocalDateTime.now()
            ));

            entityManager.getTransaction().rollback();

            Long count = entityManager.createQuery(
                    "select count(i) from IncidentEntity i", Long.class)
                    .getSingleResult();

            assertEquals(0L, count);

        } finally {
            entityManager.close();
        }
    }

    @Test
    void capabilitiesShouldBeFindableByName() {

        EntityManager entityManager = factory.createEntityManager();

        try {

            List<CapabilityEntity> capabilities = entityManager.createQuery(
                    "from CapabilityEntity order by name", CapabilityEntity.class)
                    .getResultList();

            assertEquals(3, capabilities.size());
            assertEquals("FIRE_RESPONSE", capabilities.get(0).getName());

        } finally {
            entityManager.close();
        }
    }
}