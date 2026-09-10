package com.resqgrid.service.dispatch;

import com.resqgrid.domain.dispatch.Dispatch;
import com.resqgrid.domain.dispatch.DispatchStatus;
import com.resqgrid.domain.incident.IncidentStatus;
import com.resqgrid.domain.resource.ResourceStatus;
import com.resqgrid.domain.team.TeamStatus;

public class DispatchLifecycleService {

    public void startDispatch(Dispatch dispatch) {

        validateDispatch(dispatch);

        dispatch.changeStatus(DispatchStatus.IN_PROGRESS);

        dispatch.getIncident().changeStatus(IncidentStatus.IN_PROGRESS);
    }

    public void completeDispatch(Dispatch dispatch) {

        validateDispatch(dispatch);

        dispatch.changeStatus(DispatchStatus.COMPLETED);

        dispatch.getResource().changeStatus(ResourceStatus.AVAILABLE);

        dispatch.getResponseTeam().changeStatus(TeamStatus.AVAILABLE);

        dispatch.getIncident().changeStatus(IncidentStatus.RESOLVED);
    }

    private void validateDispatch(Dispatch dispatch) {

        if (dispatch == null) {
            throw new IllegalArgumentException(
                    "Dispatch cannot be null"
            );
        }
    }
}
