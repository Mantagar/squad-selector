package org.mantagar.squadselector.player;

import lombok.RequiredArgsConstructor;
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
    public Player patchPlayer(long id, Player modified) {
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null)
            throw new PlayerNotFoundException("Player with id=%s not found".formatted(id));
        if (modified.getName() != null) player.setName(modified.getName());
        if (modified.getSurname() != null) player.setSurname(modified.getSurname());
        if (modified.getPosition() != null) player.setPosition(modified.getPosition());
        if (modified.getAvailability() != null) player.setAvailability(modified.getAvailability());
        return playerRepository.save(player);
    }

    @Transactional
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }
}
