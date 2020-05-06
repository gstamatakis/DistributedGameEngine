package ui.controller;

import exception.CustomException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import message.UserJoinQueueMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import ui.service.KafkaService;

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

    @PostMapping("/practice")
    @ApiOperation(value = "${QueueController.practice}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public String practice(@RequestBody UserJoinQueueMessage joinQueueMessage) {
        kafkaService.enqueuePractice(joinQueueMessage);
    }
}
