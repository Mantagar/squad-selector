package org.mantagar.squadselector.acceptance.steps.squad;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.List;
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
                        .defaultHeaders(h -> h.setBasicAuth("testuser", "testpass"))
                        .build();
    }

    @When("{int} players are selected")
    public void selectInvalidNumberOfPlayers(int numberOfPlayers) {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION,
                        Stream.iterate(1L, x -> x + 1).limit(numberOfPlayers).toList());
    }

    @Then("return 400 with message: 'Squad size must be 11 \\(provided: {int}\\)'")
    public void testWithInvalidSquadSize(int numberOfPlayers) {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Squad size must be 11 (provided: %d)".formatted(numberOfPlayers));
    }

    @When("selected players' positions don't match the formation")
    public void selectValidSquad_replaceGoalkeeperWithDefender() {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION, Stream.iterate(2L, x -> x + 1).limit(11).toList());
    }

    @Then("return 400 with message: 'Invalid squad composition'")
    public void testInvalidPositions() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo(
                        "Invalid squad composition:\nexactly 1 goalkeeper required\nexactly 4 defense players required");
    }

    @When("selected players contain injured and suspended ones")
    public void selectValidSquad_butSomeAreSuspendedOrInjured() {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION, List.of(1L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 12L, 13L));
    }

    @Then("return 400 with message: 'Players not available'")
    public void testSuspendedAndInjured() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo(
                        "Players not available:\nplayer with id=12 INJURED\nplayer with id=13 SUSPENDED");
    }

    @When("selected players contain duplicates")
    public void selectValidSquad_butSomeAreDuplicates() {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION, List.of(1L, 3L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 10L));
    }

    @Then("return 400 with message: 'Player ids aren't unique'")
    public void testDuplicatedPlayers() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Player ids aren't unique");
    }

    @When("selected player doesn't exist")
    public void selectValidSquad_butOneDoesNotExist() {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION, List.of(100L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));
    }

    @Then("return 404 with message: 'Invalid player ids'")
    public void testNotFoundPlayers() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Invalid player ids:\nPlayer with id=100 not found");
    }

    @When("selected players are valid")
    public void selectValidSquad() {
        createSquadRequest =
                new CreateSquadRequest(
                        TEST_FORMATION, Stream.iterate(1L, x -> x + 1).limit(11).toList());
    }

    @Then("return 201 CREATED")
    public void testValidSquad() {
        restTestClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(createSquadRequest))
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Given("players exist in the db")
    public void persistAllPlayers() {
        /*
        1 = goalkeeper
        2 - 11 = 10 valid players for formation (2,4,4)
        12 = injured defender
        13 = suspended striker
        */
        persistPlayer("Available", "Goalkeeper", Position.GOALKEEPER, Availability.AVAILABLE);
        for (int i = 1; i <= 2; i++) {
            persistPlayer("Available", "Striker " + i, Position.OFFENSE, Availability.AVAILABLE);
        }
        for (int i = 1; i <= 4; i++) {
            persistPlayer("Available", "Midfielder " + i, Position.MIDDLE, Availability.AVAILABLE);
        }
        for (int i = 1; i <= 4; i++) {
            persistPlayer("Available", "Defender " + i, Position.DEFENSE, Availability.AVAILABLE);
        }
        persistPlayer("Injured", "Striker", Position.DEFENSE, Availability.INJURED);
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
