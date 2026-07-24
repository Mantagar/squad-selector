package org.mantagar.squadselector.shared;

import org.mantagar.squadselector.player.exception.PlayerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<String> handlePlayerNotFound(PlayerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player not found");
    }
}
