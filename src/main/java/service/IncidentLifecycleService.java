package service;

import model.Incident;
import model.Status;
import repository.IncidentRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Advances an incident through the documented lifecycle.
 *
 * Only the transitions described in the incident lifecycle are accepted.
 * Attempting to skip a stage or move backwards is rejected rather than
 * silently ignored.
 */
public class IncidentLifecycleService {

    private final IncidentRepository incidentRepository;

    public IncidentLifecycleService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public void assess(long incidentId) {
        transition(incidentId, Status.REPORTED, Status.ASSESSED);
    }

    public void determineRequirements(long incidentId) {
        transition(incidentId, Status.ASSESSED, Status.REQUIREMENTS_DETERMINED);
    }

    public void startResponse(long incidentId) {
        transition(
                incidentId,
                Status.REQUIREMENTS_DETERMINED,
                Status.RESPONSE_IN_PROGRESS
        );
    }

    public void resolve(long incidentId) {
        transition(incidentId, Status.RESPONSE_IN_PROGRESS, Status.RESOLVED);
    }

    private void transition(long incidentId, Status requiredCurrent, Status newStatus) {

        try (Connection connection = database.DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                Incident incident = incidentRepository.findById(incidentId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Incident does not exist: " + incidentId
                        ));

                if (incident.getStatus() != requiredCurrent) {

                    connection.rollback();

                    throw new IllegalStateException(
                            "Incident " + incidentId + " is "
                                    + incident.getStatus() + " and cannot become "
                                    + newStatus + "."
                    );
                }

                incidentRepository.updateStatus(incidentId, newStatus, connection);

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

                throw new RuntimeException("Failed to update incident.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update incident.", e);
        }
    }
}