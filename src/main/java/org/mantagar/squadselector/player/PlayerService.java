package org.mantagar.squadselector.player;

import lombok.RequiredArgsConstructor;
import org.mantagar.squadselector.player.dto.CreatePlayerRequest;
import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.mantagar.squadselector.player.model.Player;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Iterable<Player> getPlayers() {
        return playerRepository.findAll();
    }

    @Transactional
    public Player patchPlayer(long id, CreatePlayerRequest playerRequest) {
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null)
            throw new PlayerNotFoundException("Player with id=%s not found".formatted(id));
        if (playerRequest.name() != null) player.setName(playerRequest.name());
        if (playerRequest.surname() != null) player.setSurname(playerRequest.surname());
        if (playerRequest.position() != null) player.setPosition(playerRequest.position());
        if (playerRequest.availability() != null)
            player.setAvailability(playerRequest.availability());
        return playerRepository.save(player);
    }

    @Transactional
    public Player createPlayer(CreatePlayerRequest playerRequest) {
        Player player = new Player();
        player.setName(playerRequest.name());
        player.setSurname(playerRequest.surname());
        player.setPosition(playerRequest.position());
        player.setAvailability(playerRequest.availability());
        return playerRepository.save(player);
    }
}
