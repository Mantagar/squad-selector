package org.mantagar.squadselector.squad;

import lombok.RequiredArgsConstructor;
import org.mantagar.squadselector.squad.dto.CreateSquadRequest;
import org.mantagar.squadselector.squad.exception.InvalidFormationException;
import org.mantagar.squadselector.squad.exception.InvalidSquadCompositionException;
import org.mantagar.squadselector.squad.exception.InvalidSquadSizeException;
import org.mantagar.squadselector.squad.exception.PlayerUnavailableException;
import org.mantagar.squadselector.squad.model.Squad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/squads")
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    @GetMapping
    public Iterable<Squad> getSquads() {
        return squadService.getSquads();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Squad createSquad(@RequestBody CreateSquadRequest squadRequest) {
        return squadService.createSquad(squadRequest);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidSquadSize(InvalidSquadSizeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidFormationSize(InvalidFormationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidFormationSize(InvalidSquadCompositionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidFormationSize(PlayerUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
