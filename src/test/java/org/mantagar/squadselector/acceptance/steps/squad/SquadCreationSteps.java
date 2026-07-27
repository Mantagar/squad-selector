package org.mantagar.squadselector.acceptance.steps.squad;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.stream.Stream;
import org.mantagar.squadselector.player.PlayerRepository;
import org.mantagar.squadselector.player.model.Availability;
import org.mantagar.squadselector.player.model.Player;
import org.mantagar.squadselector.player.model.Position;
import org.mantagar.squadselector.squad.dto.CreateSquadRequest;
import org.mantagar.squadselector.squad.model.Formation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SquadCreationSteps {

    private static final Formation TEST_FORMATION = new Formation(2, 4, 4);
    @LocalServerPort private int port;

    @Autowired private PlayerRepository playerRepository;

    @Autowired private ObjectMapper mapper;

    private RestTestClient restTestClient;
    private CreateSquadRequest createSquadRequest;

    @Before
    public void setup() {
        restTestClient =
                RestTestClient.bindToServer()
                        .baseUrl("http://localhost:%d/squads".formatted(port))
                        .build();
    }

    @When("{int} players are selected")
    public void selectInvalidNumberOfPlayers(int numberOfPlayers) {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION,
                        Stream.iterate(1L, x -> x + 1).limit(numberOfPlayers).toList());
    }

    @Then("return 400 with message that squad size must be 11")
    public void testWithInvalidSquadSize() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(
                        body ->
                                assertTrue(
                                        body.getResponseBody().contains("Squad size must be 11")));
    }

    @Given("players exist in the db")
    public void persistAllPlayers() {
        /*
        1 = spare goalkeeper
        2 - 12 = 11 valid players for formation (2,4,4)
        13 = spare defender
        14 = injured striker
        15 = p striker
        */
        for (int i = 1; i <= 2; i++) {
            persistPlayer(
                    "Available", "Goalkeeper " + i, Position.GOALKEEPER, Availability.AVAILABLE);
        }
        for (int i = 1; i <= 2; i++) {
            persistPlayer("Available", "Striker " + i, Position.OFFENSE, Availability.AVAILABLE);
        }
        for (int i = 1; i <= 4; i++) {
            persistPlayer("Available", "Midfielder " + i, Position.MIDDLE, Availability.AVAILABLE);
        }
        for (int i = 1; i <= 5; i++) {
            persistPlayer("Available", "Defender " + i, Position.DEFENSE, Availability.AVAILABLE);
        }
        persistPlayer("Injured", "Striker", Position.OFFENSE, Availability.INJURED);
        persistPlayer("Suspended", "Striker", Position.OFFENSE, Availability.SUSPENDED);
    }

    private void persistPlayer(
            String name, String surname, Position position, Availability availability) {
        Player p = new Player();
        p.setName(name);
        p.setSurname(surname);
        p.setPosition(position);
        p.setAvailability(availability);
        playerRepository.save(p);
    }
}
