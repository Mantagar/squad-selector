package org.mantagar.squadselector.player.dto;

import org.mantagar.squadselector.player.model.Availability;
import org.mantagar.squadselector.player.model.Position;

public record CreatePlayerRequest(
        String name, String surname, Position position, Availability availability) {}
