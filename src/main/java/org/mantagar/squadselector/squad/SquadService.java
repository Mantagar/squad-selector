package org.mantagar.squadselector.squad;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadService {

    private final SquadRepository squadRepository;
    private final PlayerRepository playerRepository;

    public Iterable<Squad> getSquads() {
        return squadRepository.findAll();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Squad createSquad(CreateSquadRequest squadRequest) {
        if (squadRequest.playerIds().size() != 11) {
            throw new InvalidSquadSizeException(
                    "Squad size must be 11 (provided: " + squadRequest.playerIds().size() + ")");
        }
        List<Player> players = playerRepository.findAllById(squadRequest.playerIds());
        verifyPlayerIds(players, squadRequest.playerIds());
        verifyPlayerPositions(players, squadRequest.formation());
        verifyPlayerAvailability(players);
        Squad squad = new Squad();
        squad.setFormation(squadRequest.formation());
        squad.setPlayers(players);
        return squadRepository.save(squad);
    }

    private void verifyPlayerAvailability(List<Player> players) {
        StringBuilder exceptionMsg = new StringBuilder();
        players.stream()
                .filter(player -> player.getAvailability() != Availability.AVAILABLE)
                .forEach(
                        player ->
                                exceptionMsg.append(
                                        "\nplayer with id=%d %s"
                                                .formatted(
                                                        player.getId(),
                                                        player.getAvailability().name())));
        if (!exceptionMsg.isEmpty())
            throw new PlayerUnavailableException("Players not available:" + exceptionMsg);
    }

    private void verifyPlayerPositions(List<Player> players, Formation formation) {
        StringBuilder exceptionMsg = new StringBuilder();
        Map<Position, Long> positionToCount =
                players.stream()
                        .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));
        if (positionToCount.getOrDefault(Position.GOALKEEPER, 0L) != 1L)
            exceptionMsg.append("\nexactly 1 goalkeeper required");
        if (positionToCount.getOrDefault(Position.OFFENSE, 0L) != formation.getOffense())
            exceptionMsg
                    .append("\nexactly ")
                    .append(formation.getOffense())
                    .append(" offense players required");
        if (positionToCount.getOrDefault(Position.MIDDLE, 0L) != formation.getMiddle())
            exceptionMsg
                    .append("\nexactly ")
                    .append(formation.getMiddle())
                    .append(" middle players required");
        if (positionToCount.getOrDefault(Position.DEFENSE, 0L) != formation.getDefense())
            exceptionMsg
                    .append("\nexactly ")
                    .append(formation.getDefense())
                    .append(" defense players required");
        if (!exceptionMsg.isEmpty())
            throw new InvalidSquadCompositionException("Invalid squad composition:" + exceptionMsg);
    }

    private void verifyPlayerIds(List<Player> fetchedPlayers, List<Long> requestedPlayerIds) {
        if (fetchedPlayers.size() < 11) {
            StringBuilder exceptionMsg = new StringBuilder();
            List<Long> fetchedIds = fetchedPlayers.stream().map(Player::getId).toList();
            // find all player ids that weren't fetched
            requestedPlayerIds.stream()
                    .filter(id -> !fetchedIds.contains(id))
                    .forEach(
                            id ->
                                    exceptionMsg
                                            .append("\nPlayer with id=")
                                            .append(id)
                                            .append(" not found"));
            if (!exceptionMsg.isEmpty())
                throw new PlayerNotFoundException("Invalid player ids:" + exceptionMsg);
            else throw new InvalidSquadSizeException("Player ids aren't unique");
        }
    }
}
