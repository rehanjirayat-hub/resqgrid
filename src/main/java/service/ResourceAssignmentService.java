package service;

import model.Dispatch;
import model.EmergencyResource;
import model.Incident;
import model.ResourceStatus;
import model.ResponseTeam;
import repository.DispatchRepository;
import repository.ResourceRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates a dispatch and marks the assigned resource busy as a single
 * database operation.
 *
 * The resource transition is conditional, so a resource that was assigned by
 * another transaction after it was selected can no longer be assigned here.
 * When that happens nothing is committed and the caller is told the resource
 * was already claimed.
 */
public class ResourceAssignmentService {

    private final DispatchRepository dispatchRepository;
    private final ResourceRepository resourceRepository;

    public ResourceAssignmentService(DispatchRepository dispatchRepository,
                                     ResourceRepository resourceRepository) {

        this.dispatchRepository = dispatchRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * @return the new dispatch ID, or empty when the resource was no longer
     *         available for assignment
     */
    public AssignmentResult assign(Incident incident,
                                   EmergencyResource resource,
                                   ResponseTeam responseTeam) {

        try (Connection connection = database.DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                boolean claimed = resourceRepository.transitionStatusIfCurrent(
                        resource.getId(),
                        ResourceStatus.AVAILABLE,
                        ResourceStatus.BUSY,
                        connection
                );

                if (!claimed) {
                    connection.rollback();

                    return AssignmentResult.rejected(resource.getId());
                }

                long dispatchId = dispatchRepository.save(
                        new Dispatch(0, incident, resource, responseTeam),
                        connection
                );

                connection.commit();

                return AssignmentResult.assigned(dispatchId, resource.getId());

            } catch (SQLException e) {

                connection.rollback();

                throw new RuntimeException("Failed to assign resource.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign resource.", e);
        }
    }
}