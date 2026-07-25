package org.mantagar.squadselector.squad;

import lombok.RequiredArgsConstructor;
import org.mantagar.squadselector.squad.model.Squad;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Squad postSquad(@RequestBody Squad squad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
