package org.mantagar.squadselector.squad.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mantagar.squadselector.squad.exception.InvalidFormationException;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Formation {
    private int offense;
    private int middle;
    private int defense;

    public Formation(int offense, int middle, int defense) {
        StringBuilder exceptionMsg = new StringBuilder();
        if (offense < 1)
            exceptionMsg
                    .append("\noffense has to be at least 1 (provided: ")
                    .append(offense)
                    .append(")");
        if (middle < 1)
            exceptionMsg
                    .append("\nmiddle has to be at least 1 (provided: ")
                    .append(middle)
                    .append(")");
        if (defense < 1)
            exceptionMsg
                    .append("\ndefense has to be at least 1 (provided: ")
                    .append(defense)
                    .append(")");
        if (offense + defense + middle != 10)
            exceptionMsg
                    .append("\noffense, middle, and defense must be 10 in total (provided: ")
                    .append(offense + defense + middle)
                    .append(")");
        if (!exceptionMsg.isEmpty())
            throw new InvalidFormationException("Invalid formation:" + exceptionMsg);
        this.offense = offense;
        this.middle = middle;
        this.defense = defense;
    }
}
