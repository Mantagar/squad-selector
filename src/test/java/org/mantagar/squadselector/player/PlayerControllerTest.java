package org.mantagar.squadselector.player;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mantagar.squadselector.player.dto.CreatePlayerRequest;
import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.mantagar.squadselector.player.model.Availability;
import org.mantagar.squadselector.player.model.Player;
import org.mantagar.squadselector.player.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PlayerService playerService;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /players should return all players")
    void getPlayers() throws Exception {
        Player player1 = new Player();
        player1.setId(1L);
        player1.setName("John");
        player1.setSurname("Doe");
        player1.setPosition(Position.OFFENSE);
        player1.setAvailability(Availability.AVAILABLE);

        Player player2 = new Player();
        player2.setId(2L);
        player2.setName("Jane");
        player2.setSurname("Smith");
        player2.setPosition(Position.MIDDLE);
        player2.setAvailability(Availability.INJURED);

        when(playerService.getPlayers()).thenReturn(List.of(player1, player2));

        mockMvc.perform(get("/players").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane"));

        verify(playerService, times(1)).getPlayers();
    }

    @Test
    @DisplayName("POST /players should create a new player and return 201 CREATED")
    void createPlayer() throws Exception {
        CreatePlayerRequest newPlayer =
                new CreatePlayerRequest(
                        "Mike", "Johnson", Position.DEFENSE, Availability.AVAILABLE);

        Player createdPlayer = new Player();
        createdPlayer.setId(1L);
        createdPlayer.setName("Mike");
        createdPlayer.setSurname("Johnson");
        createdPlayer.setPosition(Position.DEFENSE);
        createdPlayer.setAvailability(Availability.AVAILABLE);

        when(playerService.createPlayer(any(CreatePlayerRequest.class))).thenReturn(createdPlayer);

        mockMvc.perform(
                        post("/players")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newPlayer)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mike"))
                .andExpect(jsonPath("$.position").value("DEFENSE"));

        verify(playerService, times(1)).createPlayer(any(CreatePlayerRequest.class));
    }

    @Test
    @DisplayName("PATCH /players/{id} should update an existing player")
    void patchPlayer_playerExists_updatesPlayer() throws Exception {
        Player updatedPlayer = new Player();
        updatedPlayer.setId(1L);
        updatedPlayer.setName("Michael");
        updatedPlayer.setSurname("Doe");
        updatedPlayer.setPosition(Position.OFFENSE);
        updatedPlayer.setAvailability(Availability.INJURED);

        when(playerService.patchPlayer(eq(1L), any(CreatePlayerRequest.class)))
                .thenReturn(updatedPlayer);

        mockMvc.perform(
                        patch("/players/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"availability\":\"INJURED\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Michael"))
                .andExpect(jsonPath("$.availability").value("INJURED"));

        verify(playerService, times(1)).patchPlayer(eq(1L), any(CreatePlayerRequest.class));
    }

    @Test
    @DisplayName("PATCH /players/{id} should return 404 when player not found")
    void patchPlayer_playerNotFound_returns404() throws Exception {
        CreatePlayerRequest patchPlayer =
                new CreatePlayerRequest(
                        "Mike", "Johnson", Position.DEFENSE, Availability.AVAILABLE);

        when(playerService.patchPlayer(eq(1L), any(CreatePlayerRequest.class)))
                .thenThrow(new PlayerNotFoundException("Player with id=1 not found"));

        mockMvc.perform(
                        patch("/players/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchPlayer)))
                .andExpect(status().isNotFound());

        verify(playerService, times(1)).patchPlayer(eq(1L), any(CreatePlayerRequest.class));
    }

    @Test
    @DisplayName("POST /players should handle invalid input")
    void createPlayer_invalidInput() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(
                        post("/players")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
