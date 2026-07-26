package org.mantagar.squadselector.player;

import java.util.List;
import org.mantagar.squadselector.player.model.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> {

    List<Player> findAllById(Iterable<Long> ids);
}
