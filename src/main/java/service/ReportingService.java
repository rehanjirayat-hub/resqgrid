package service;

import model.EmergencyResource;
import repository.ReportRepository;
import repository.ResourceRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces the operational reports defined in the Business Rules document.
 *
 * Reporting stays separate from dispatch-selection logic: this class only
 * summarises stored records and never influences which resource is chosen.
 */
public class ReportingService {

    private final ReportRepository reportRepository;
    private final ResourceRepository resourceRepository;

    public ReportingService(ReportRepository reportRepository,
                            ResourceRepository resourceRepository) {

        this.reportRepository = reportRepository;
        this.resourceRepository = resourceRepository;
    }

    public IncidentStatistics incidentStatistics() {

        return new IncidentStatistics(
                reportRepository.countIncidents(),
                reportRepository.countIncidentsByStatus(),
                reportRepository.countIncidentsBySeverity()
        );
    }

    public DispatchStatistics dispatchStatistics() {

        return new DispatchStatistics(
                reportRepository.countDispatches(),
                reportRepository.countActiveDispatches(),
                reportRepository.countDispatchesByStatus()
        );
    }

    public ResourceUtilization resourceUtilization() {

        Map<String, Long> byStatus = reportRepository.countResourcesByStatus();

        return new ResourceUtilization(
                reportRepository.countResources(),
                byStatus,
                reportRepository.countResourcesNotAvailable()
        );
    }

    /**
     * Workload per resource. Resources without dispatch history are reported
     * with a workload of zero rather than being left out.
     */
    public Map<Long, Long> resourceWorkload() {

        Map<Long, Long> workload = new LinkedHashMap<>();

        for (EmergencyResource resource
                : resourceRepository.findAll()) {

            workload.put(
                    resource.getId(),
                    resourceRepository.countDispatches(resource.getId())
            );
        }

        return workload;
    }

    public Map<Long, Long> completedDispatchesPerResource() {

        Map<Long, Long> completed = new LinkedHashMap<>();

        for (EmergencyResource resource
                : resourceRepository.findAll()) {

            completed.put(
                    resource.getId(),
                    reportRepository.countCompletedDispatchesPerResource(
                            resource.getId()
                    )
            );
        }

        return completed;
    }

    public record IncidentStatistics(long totalIncidents,
                                     Map<String, Long> byStatus,
                                     Map<String, Long> bySeverity) {
    }

    public record DispatchStatistics(long totalDispatches,
                                     long activeDispatches,
                                     Map<String, Long> byStatus) {
    }

    public record ResourceUtilization(long totalResources,
                                       Map<String, Long> byStatus,
                                       long resourcesNotAvailable) {
    }
}