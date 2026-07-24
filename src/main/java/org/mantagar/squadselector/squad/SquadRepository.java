package org.mantagar.squadselector.squad;

import org.mantagar.squadselector.squad.model.Squad;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquadRepository extends CrudRepository<Squad, Long> {}
