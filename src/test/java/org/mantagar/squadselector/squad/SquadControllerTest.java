package org.mantagar.squadselector.squad;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.mantagar.squadselector.squad.dto.CreateSquadRequest;
import org.mantagar.squadselector.squad.exception.InvalidFormationException;
import org.mantagar.squadselector.squad.exception.InvalidSquadCompositionException;
import org.mantagar.squadselector.squad.exception.InvalidSquadSizeException;
import org.mantagar.squadselector.squad.exception.PlayerUnavailableException;
import org.mantagar.squadselector.squad.model.Formation;
import org.mantagar.squadselector.squad.model.Squad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SquadController.class)
class SquadControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SquadService squadService;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /squads should return all squads")
    void getSquads() throws Exception {
        Squad s1 = new Squad();
        s1.setId(1L);
        s1.setFormation(new Formation(4, 3, 3));

        Squad s2 = new Squad();
        s2.setId(2L);
        s2.setFormation(new Formation(3, 4, 3));

        when(squadService.getSquads()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/squads").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(squadService, times(1)).getSquads();
    }

    @Test
    @DisplayName("POST /squads should create a new squad and return 201 CREATED")
    void createSquad() throws Exception {
        CreateSquadRequest req =
                new CreateSquadRequest(
                        new Formation(4, 3, 3),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        Squad created = new Squad();
        created.setId(1L);
        created.setFormation(new Formation(4, 3, 3));

        when(squadService.createSquad(any(CreateSquadRequest.class))).thenReturn(created);

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.formation.offense").value(4));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should return 400 when squad size invalid")
    void createSquad_invalidSize() throws Exception {
        CreateSquadRequest req = new CreateSquadRequest(new Formation(4, 3, 3), List.of(1L, 2L));

        when(squadService.createSquad(any(CreateSquadRequest.class)))
                .thenThrow(new InvalidSquadSizeException("Squad size must be 11"));

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Squad size must be 11"));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should return 400 when formation invalid")
    void createSquad_invalidFormation() throws Exception {
        CreateSquadRequest req =
                new CreateSquadRequest(
                        new Formation(4, 3, 3),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        when(squadService.createSquad(any(CreateSquadRequest.class)))
                .thenThrow(new InvalidFormationException("Invalid formation"));

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid formation"));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should return 400 when players unavailable")
    void createSquad_playersUnavailable() throws Exception {
        CreateSquadRequest req =
                new CreateSquadRequest(
                        new Formation(4, 3, 3),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        when(squadService.createSquad(any(CreateSquadRequest.class)))
                .thenThrow(new PlayerUnavailableException("Players not available"));

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Players not available"));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should return 404 when players not found")
    void createSquad_playersNotFound() throws Exception {
        CreateSquadRequest req =
                new CreateSquadRequest(
                        new Formation(4, 3, 3),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        when(squadService.createSquad(any(CreateSquadRequest.class)))
                .thenThrow(new PlayerNotFoundException("Players not found"));

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Players not found"));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should return 400 when players positions don't match the formation")
    void createSquad_playersInvalidComposition() throws Exception {
        CreateSquadRequest req =
                new CreateSquadRequest(
                        new Formation(4, 3, 3),
                        List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        when(squadService.createSquad(any(CreateSquadRequest.class)))
                .thenThrow(new InvalidSquadCompositionException("Invalid squad composition"));

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid squad composition"));

        verify(squadService, times(1)).createSquad(any(CreateSquadRequest.class));
    }

    @Test
    @DisplayName("POST /squads should handle invalid JSON input")
    void createSquad_invalidJson() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(
                        post("/squads")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
