package service;

import model.DispatchStatus;
import model.ResourceStatus;
import repository.DispatchRepository;
import repository.ResourceRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Moves a dispatch through its lifecycle and keeps the operational state of
 * the assigned resource consistent with it.
 *
 * The dispatch status change and the resource state change are committed
 * together, so a dispatch can never be marked completed while its resource
 * stays busy, nor completed while the resource is released.
 */
public class DispatchLifecycleService {

    private final DispatchRepository dispatchRepository;
    private final ResourceRepository resourceRepository;

    public DispatchLifecycleService(DispatchRepository dispatchRepository,
                                    ResourceRepository resourceRepository) {

        this.dispatchRepository = dispatchRepository;
        this.resourceRepository = resourceRepository;
    }

    public void start(long dispatchId) {
        changeStatus(dispatchId, DispatchStatus.CREATED, DispatchStatus.IN_PROGRESS);
    }

    /**
     * Completes the dispatch and returns the assigned resource to
     * {@code AVAILABLE}, because a completed dispatch is historical and does
     * not keep the resource committed.
     */
    public void complete(long dispatchId) {
        changeStatus(
                dispatchId,
                DispatchStatus.IN_PROGRESS,
                DispatchStatus.COMPLETED
        );
    }

    private void changeStatus(long dispatchId,
                              DispatchStatus requiredCurrentStatus,
                              DispatchStatus newStatus) {

        try (Connection connection = database.DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                var dispatch = dispatchRepository.findById(dispatchId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Dispatch does not exist: " + dispatchId
                        ));

                if (dispatch.getStatus() != requiredCurrentStatus) {

                    connection.rollback();

                    throw new IllegalStateException(
                            "Dispatch " + dispatchId + " is "
                                    + dispatch.getStatus() + " and cannot become "
                                    + newStatus + "."
                    );
                }

                dispatchRepository.updateStatus(dispatchId, newStatus, connection);

                if (newStatus == DispatchStatus.COMPLETED) {
                    resourceRepository.updateStatus(
                            dispatch.getResource().getId(),
                            ResourceStatus.AVAILABLE,
                            connection
                    );
                }

                connection.commit();

            } catch (SQLException e) {

                connection.rollback();

                throw new RuntimeException("Failed to update dispatch.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update dispatch.", e);
        }
    }
}