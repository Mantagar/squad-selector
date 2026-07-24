package org.mantagar.squadselector.squad.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mantagar.squadselector.player.model.Player;

@Entity
@NoArgsConstructor
@Setter()
@Getter()
public class Squad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "squad_seq_generator")
    @SequenceGenerator(name = "squad_seq_generator", sequenceName = "squad_seq_generator")
    private long id;

    @Embedded private Formation formation;

    @ManyToMany(mappedBy = "squad_to_player")
    private List<Player> players;
}
