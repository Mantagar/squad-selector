package org.mantagar.squadselector.squad.dto;

import java.util.List;
import org.mantagar.squadselector.squad.model.Formation;

public record CreateSquadRequest(Formation formation, List<Long> playerIds) {}
