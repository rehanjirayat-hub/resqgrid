package com.resqgrid.service.dispatch;

import com.resqgrid.domain.dispatch.Dispatch;
import com.resqgrid.domain.dispatch.DispatchStatus;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.team.TeamStatus;
import com.resqgrid.service.history.HistoryService;

public class DispatchLifecycleService {

    private final HistoryService historyService;

    public DispatchLifecycleService(HistoryService historyService) {

        if (historyService == null) {
            throw new IllegalArgumentException(
                    "History service cannot be null"
            );
        }

        this.historyService = historyService;
    }

    public void startDispatch(Dispatch dispatch) {

        validateDispatch(dispatch);

        dispatch.changeStatus(
                DispatchStatus.IN_PROGRESS
        );

        dispatch.getIncident().changeStatus(
                IncidentStatus.IN_PROGRESS
        );

        historyService.record(
                dispatch.getId(),
                "DISPATCH",
                "STARTED",
                "Dispatch started for incident "
                        + dispatch.getIncident().getId()
        );
    }

    public void completeDispatch(Dispatch dispatch) {

        validateDispatch(dispatch);

        dispatch.changeStatus(
                DispatchStatus.COMPLETED
        );

        dispatch.getResource().changeStatus(
                ResourceStatus.AVAILABLE
        );

        dispatch.getResponseTeam().changeStatus(
                TeamStatus.AVAILABLE
        );

        dispatch.getIncident().changeStatus(
                IncidentStatus.RESOLVED
        );

        historyService.record(
                dispatch.getId(),
                "DISPATCH",
                "COMPLETED",
                "Dispatch completed for incident "
                        + dispatch.getIncident().getId()
        );
    }

    public void cancelDispatch(Dispatch dispatch) {

        validateDispatch(dispatch);

        dispatch.changeStatus(
                DispatchStatus.CANCELLED
        );

        if (dispatch.getResource() != null) {
            dispatch.getResource().changeStatus(
                    ResourceStatus.AVAILABLE
            );
        }

        if (dispatch.getResponseTeam() != null) {
            dispatch.getResponseTeam().changeStatus(
                    TeamStatus.AVAILABLE
            );
        }

        dispatch.getIncident().changeStatus(
                IncidentStatus.CANCELLED
        );

        historyService.record(
                dispatch.getId(),
                "DISPATCH",
                "CANCELLED",
                "Dispatch cancelled for incident "
                        + dispatch.getIncident().getId()
        );
    }

    private void validateDispatch(Dispatch dispatch) {

        if (dispatch == null) {
            throw new IllegalArgumentException(
                    "Dispatch cannot be null"
            );
        }
    }
}
