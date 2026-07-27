package org.mantagar.squadselector.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mantagar.squadselector.player.dto.CreatePlayerRequest;
import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.mantagar.squadselector.player.model.Availability;
import org.mantagar.squadselector.player.model.Player;
import org.mantagar.squadselector.player.model.Position;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock private PlayerRepository playerRepository;

    @InjectMocks private PlayerService playerService;

    @Test
    @DisplayName("getPlayers should return all players from repository")
    void getPlayers_returnsAllPlayers() {
        Player player1 = createPlayer(1L, "John", "Doe", Position.OFFENSE, Availability.AVAILABLE);
        Player player2 = createPlayer(2L, "Jane", "Smith", Position.MIDDLE, Availability.INJURED);
        Player player3 =
                createPlayer(3L, "Mike", "Johnson", Position.DEFENSE, Availability.AVAILABLE);

        when(playerRepository.findAll()).thenReturn(List.of(player1, player2, player3));

        Iterable<Player> result = playerService.getPlayers();

        assertEquals(3, ((List<Player>) result).size());
        verify(playerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createPlayer should create and return a new player")
    void createPlayer_createsPlayer() {
        CreatePlayerRequest request =
                new CreatePlayerRequest("John", "Doe", Position.OFFENSE, Availability.AVAILABLE);
        Player expectedPlayer =
                createPlayer(1L, "John", "Doe", Position.OFFENSE, Availability.AVAILABLE);

        when(playerRepository.save(any(Player.class))).thenReturn(expectedPlayer);

        Player result = playerService.createPlayer(request);

        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());
        assertEquals(Position.OFFENSE, result.getPosition());
        assertEquals(Availability.AVAILABLE, result.getAvailability());
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("patchPlayer should update player when player exists")
    void patchPlayer_updatesPlayer() {
        long playerId = 1L;
        Player existingPlayer =
                createPlayer(playerId, "John", "Doe", Position.OFFENSE, Availability.AVAILABLE);
        Player updatedPlayer =
                createPlayer(playerId, "John", "Smith", Position.MIDDLE, Availability.INJURED);

        CreatePlayerRequest updateRequest =
                new CreatePlayerRequest("John", "Smith", Position.MIDDLE, Availability.INJURED);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(updatedPlayer);

        Player result = playerService.patchPlayer(playerId, updateRequest);

        assertEquals("Smith", result.getSurname());
        assertEquals(Position.MIDDLE, result.getPosition());
        assertEquals(Availability.INJURED, result.getAvailability());
        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("patchPlayer should update only provided fields")
    void patchPlayer_updatesOnlyProvidedFields() {
        long playerId = 1L;
        Player existingPlayer =
                createPlayer(playerId, "John", "Doe", Position.OFFENSE, Availability.AVAILABLE);

        CreatePlayerRequest partialUpdateRequest =
                new CreatePlayerRequest("John", null, null, Availability.INJURED);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(existingPlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(existingPlayer);

        playerService.patchPlayer(playerId, partialUpdateRequest);

        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @DisplayName("patchPlayer should throw PlayerNotFoundException when player not found")
    void patchPlayer_playerDoesNotExist_throwsPlayerNotFoundException() {
        long playerId = 999L;
        CreatePlayerRequest request =
                new CreatePlayerRequest("John", "Doe", Position.OFFENSE, Availability.AVAILABLE);

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(
                PlayerNotFoundException.class, () -> playerService.patchPlayer(playerId, request));

        verify(playerRepository, times(1)).findById(playerId);
        verify(playerRepository, times(0)).save(any(Player.class));
    }

    @Test
    @DisplayName("getPlayers should return empty list when no players exist")
    void getPlayers_returnsEmptyList() {
        when(playerRepository.findAll()).thenReturn(List.of());

        Iterable<Player> result = playerService.getPlayers();

        assertEquals(0, ((List<Player>) result).size());
        verify(playerRepository, times(1)).findAll();
    }

    private Player createPlayer(
            Long id, String name, String surname, Position position, Availability availability) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setSurname(surname);
        player.setPosition(position);
        player.setAvailability(availability);
        return player;
    }
}
