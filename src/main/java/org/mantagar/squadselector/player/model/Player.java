package org.mantagar.squadselector.player.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_seq_generator")
    @SequenceGenerator(name = "player_seq_generator", sequenceName = "player_seq_generator")
    private long id;

    private String name;
    private String surname;
    private Position position;
    private Availability availability;
}
