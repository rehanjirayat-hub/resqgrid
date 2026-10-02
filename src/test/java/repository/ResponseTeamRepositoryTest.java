package repository;

import model.ResponseTeam;
import model.ResponseTeamStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseTeamRepositoryTest {

    private final ResponseTeamRepository repository = new ResponseTeamRepository();

    @Test
    void saveShouldInsertResponseTeamAndReturnGeneratedId() {

        long id = repository.save(new ResponseTeam(0));

        assertTrue(id > 0);
    }

    @Test
    void findByIdShouldReturnSavedResponseTeam() {

        long id = repository.save(new ResponseTeam(0));

        Optional<ResponseTeam> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals(ResponseTeamStatus.AVAILABLE, result.get().getStatus());
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {

        assertTrue(repository.findById(-1).isEmpty());
    }

    @Test
    void findByStatusShouldReturnOnlyMatchingTeams() {

        long id = repository.save(new ResponseTeam(0));

        List<ResponseTeam> result =
                repository.findByStatus(ResponseTeamStatus.AVAILABLE);

        assertTrue(result.stream().anyMatch(team -> team.getId() == id));
    }

    @Test
    void updateStatusShouldChangeStoredStatus() {

        long id = repository.save(new ResponseTeam(0));

        repository.updateStatus(id, ResponseTeamStatus.BUSY);

        ResponseTeam updated = repository.findById(id).orElseThrow();

        assertEquals(ResponseTeamStatus.BUSY, updated.getStatus());
        assertTrue(repository.findByStatus(ResponseTeamStatus.AVAILABLE)
                               .stream()
                               .noneMatch(team -> team.getId() == id));
    }

    @Test
    void updateStatusShouldRejectUnknownTeam() {

        assertThrows(
                RuntimeException.class,
                () -> repository.updateStatus(-1, ResponseTeamStatus.OFFLINE)
        );
    }
}
