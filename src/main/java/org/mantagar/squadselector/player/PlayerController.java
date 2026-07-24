package org.mantagar.squadselector.player;

import lombok.RequiredArgsConstructor;
import org.mantagar.squadselector.player.model.Player;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public Iterable<Player> getPlayers() {
        return playerService.getPlayers();
    }

    @PatchMapping("/{id}")
    public Player patchPlayer(@RequestBody Player player, @PathVariable long id) {
        return playerService.patchPlayer(id, player);
    }

    // TODO idempotency key
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);
    }
}
