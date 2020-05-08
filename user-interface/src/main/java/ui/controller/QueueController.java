package ui.controller;

import exception.CustomException;
import game.GameType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import message.JoinPlayMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import ui.service.KafkaService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/queue")
@Api(tags = "queue")
public class QueueController {

    @Autowired
    private KafkaService kafkaService;

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<String> handleConflict(CustomException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @PostMapping("/practice/{gameType}")
    @ApiOperation(value = "${QueueController.practice}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public ResponseEntity<String> practice(@PathVariable String gameType,
                                           @AuthenticationPrincipal UserDetails userDetails) throws InterruptedException, ExecutionException, TimeoutException {
        GameType gt = GameType.valueOf(gameType);
        kafkaService.enqueuePractice(userDetails.getUsername(), gt);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @PostMapping("/tournament/{gameType}")
    @ApiOperation(value = "${QueueController.tournament}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public ResponseEntity<String> tournament(@RequestBody JoinPlayMessage joinQueueMessage,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable String gameType) {
        GameType gt = GameType.valueOf(gameType);
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
