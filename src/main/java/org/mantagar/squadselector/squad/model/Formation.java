package org.mantagar.squadselector.squad.model;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public final class Formation {
    private final int offense;
    private final int middle;
    private final int defense;

    public Formation(int offense, int middle, int defense) {
        if (offense < 1 || middle < 1 || defense < 1)
            throw new IllegalArgumentException(
                    "Each position should be occupied by at least 1 player");
        if (offense + defense + middle != 10)
            throw new IllegalArgumentException("Combined the formation should equal 10 players");
        this.offense = offense;
        this.middle = middle;
        this.defense = defense;
    }
}
