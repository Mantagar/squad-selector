package org.mantagar.squadselector.player;

import org.mantagar.squadselector.player.model.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface PlayerRepository extends CrudRepository<Player, Long> {}
