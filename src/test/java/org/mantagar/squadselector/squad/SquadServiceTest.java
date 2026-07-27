package org.mantagar.squadselector.squad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mantagar.squadselector.player.PlayerRepository;
import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.mantagar.squadselector.player.model.Availability;
import org.mantagar.squadselector.player.model.Player;
import org.mantagar.squadselector.player.model.Position;
import org.mantagar.squadselector.squad.dto.CreateSquadRequest;
import org.mantagar.squadselector.squad.exception.InvalidSquadCompositionException;
import org.mantagar.squadselector.squad.exception.InvalidSquadSizeException;
import org.mantagar.squadselector.squad.exception.PlayerUnavailableException;
import org.mantagar.squadselector.squad.model.Formation;
import org.mantagar.squadselector.squad.model.Squad;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SquadServiceTest {

    @Mock private SquadRepository squadRepository;

    @Mock private PlayerRepository playerRepository;

    @InjectMocks private SquadService squadService;

    @Test
    @DisplayName("getSquads should return all squads from repository")
    void getSquads_returnsAllSquads() {
        Squad squad1 = new Squad();
        squad1.setId(1L);
        squad1.setFormation(new Formation(4, 3, 3));

        Squad squad2 = new Squad();
        squad2.setId(2L);
        squad2.setFormation(new Formation(3, 4, 3));

        when(squadRepository.findAll()).thenReturn(List.of(squad1, squad2));

        Iterable<Squad> result = squadService.getSquads();

        assertEquals(2, ((List<Squad>) result).size());
        verify(squadRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createSquad should create a valid squad with correct composition")
    void createSquad_createsValidSquad() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        List<Player> players = createValidSquadPlayers(formation);
        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        Squad expectedSquad = new Squad();
        expectedSquad.setId(1L);
        expectedSquad.setFormation(formation);
        expectedSquad.setPlayers(players);

        when(playerRepository.findAllById(playerIds)).thenReturn(players);
        when(squadRepository.save(any(Squad.class))).thenReturn(expectedSquad);

        Squad result = squadService.createSquad(request);

        assertEquals(1L, result.getId());
        assertEquals(formation, result.getFormation());
        verify(squadRepository, times(1)).save(any(Squad.class));
    }

    @Test
    @DisplayName("createSquad should throw InvalidSquadSizeException when squad size is not 11")
    void createSquad_invalidNumberOfPlayers_throwsInvalidSquadSizeException() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds = List.of(1L, 2L); // Only 2 players

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        InvalidSquadSizeException exception =
                assertThrows(
                        InvalidSquadSizeException.class, () -> squadService.createSquad(request));

        assertTrue(exception.getMessage().contains("Squad size must be 11"));
    }

    @Test
    @DisplayName("createSquad should throw PlayerNotFoundException when player not found")
    void createSquad_playerDoesNotExist_throwsPlayerNotFoundException() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 999L);

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        // Return 10 players instead of 11 (missing player 999L)
        List<Player> players = createValidSquadPlayers(formation).subList(0, 10);

        when(playerRepository.findAllById(playerIds)).thenReturn(players);

        assertThrows(PlayerNotFoundException.class, () -> squadService.createSquad(request));

        verify(squadRepository, times(0)).save(any(Squad.class));
    }

    @Test
    @DisplayName(
            "createSquad should throw InvalidSquadSizeException when player ids are not unique")
    void createSquad_playerIdsNotUnique_throwsInvalidSquadSizeException() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds =
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 1L); // 1L duplicated

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        List<Player> players =
                createValidSquadPlayers(formation).subList(0, 10); // Only 10 unique players

        when(playerRepository.findAllById(playerIds)).thenReturn(players);

        InvalidSquadSizeException exception =
                assertThrows(
                        InvalidSquadSizeException.class, () -> squadService.createSquad(request));

        assertTrue(exception.getMessage().contains("Player ids aren't unique"));
    }

    @Test
    @DisplayName("createSquad should throw PlayerUnavailableException when player is not available")
    void createSquad_playerUnavailable_throwsPlayerUnavailableException() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        List<Player> players = createValidSquadPlayers(formation);
        players.getFirst().setAvailability(Availability.INJURED); // Make one player unavailable

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        when(playerRepository.findAllById(playerIds)).thenReturn(players);

        PlayerUnavailableException exception =
                assertThrows(
                        PlayerUnavailableException.class, () -> squadService.createSquad(request));

        assertTrue(exception.getMessage().contains("Players not available"));
        verify(squadRepository, times(0)).save(any(Squad.class));
    }

    @Test
    @DisplayName(
            "createSquad should throw InvalidSquadCompositionException when goalkeeper count is invalid")
    void createSquad_noGoalkeeper_throwsInvalidSquadCompositionException() {
        Formation formation = new Formation(4, 3, 3);
        List<Long> playerIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        List<Player> players = createValidSquadPlayers(formation);
        players.getFirst().setPosition(Position.OFFENSE); // No goalkeeper

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        when(playerRepository.findAllById(playerIds)).thenReturn(players);

        InvalidSquadCompositionException exception =
                assertThrows(
                        InvalidSquadCompositionException.class,
                        () -> squadService.createSquad(request));

        assertTrue(exception.getMessage().contains("exactly 1 goalkeeper required"));
    }

    @Test
    @DisplayName(
            "createSquad should throw InvalidSquadCompositionException when offense count is invalid")
    void createSquad_compositionMismatch_throwsInvalidSquadCompositionException() {
        Formation formation = new Formation(2, 4, 4); // Requires 2 offense
        List<Long> playerIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        List<Player> players = createValidSquadPlayers(formation);
        players.get(1).setPosition(Position.DEFENSE); // Wrong position

        CreateSquadRequest request = new CreateSquadRequest(formation, playerIds);

        when(playerRepository.findAllById(playerIds)).thenReturn(players);

        InvalidSquadCompositionException exception =
                assertThrows(
                        InvalidSquadCompositionException.class,
                        () -> squadService.createSquad(request));

        assertTrue(exception.getMessage().contains("exactly 2 offense players required"));
    }

    @Test
    @DisplayName("getSquads should return empty list when no squads exist")
    void getSquads_shouldReturnEmptyList() {
        when(squadRepository.findAll()).thenReturn(List.of());

        Iterable<Squad> result = squadService.getSquads();

        assertEquals(0, ((List<Squad>) result).size());
        verify(squadRepository, times(1)).findAll();
    }

    private List<Player> createValidSquadPlayers(Formation formation) {
        List<Player> players = new ArrayList<>();
        players.add(createPlayer(1L, "Goalkeeper", Position.GOALKEEPER));
        for (int i = 2; i < 2 + formation.getOffense(); i++) {
            players.add(createPlayer((long) i, "Striker", Position.OFFENSE));
        }
        int middleStart = 2 + formation.getOffense();
        for (int i = middleStart; i < middleStart + formation.getMiddle(); i++) {
            players.add(createPlayer((long) i, "Midfielder", Position.MIDDLE));
        }
        int defenseStart = middleStart + formation.getMiddle();
        for (int i = defenseStart; i < defenseStart + formation.getDefense(); i++) {
            players.add(createPlayer((long) i, "Defender", Position.DEFENSE));
        }
        return players;
    }

    private Player createPlayer(Long id, String name, Position position) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setPosition(position);
        player.setAvailability(Availability.AVAILABLE);
        return player;
    }
}
