package ui.controller;

import dto.PlayStateResponseDTO;
import exception.CustomException;
import io.swagger.annotations.*;
import message.created.PlayMessage;
import message.requests.RequestCreateTournamentMessage;
import message.requests.RequestPracticeMessage;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import ui.service.PlayService;

import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/queue")
@Api(tags = "queue")
public class QueueController {

    @Autowired
    private PlayService playService;

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @Autowired
    private ModelMapper modelMapper;

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
    public ResponseEntity<String> practice(@ApiParam("Queue for practice") @RequestBody RequestPracticeMessage msg,
                                           @AuthenticationPrincipal UserDetails userDetails)
            throws InterruptedException, ExecutionException, TimeoutException {

        playService.enqueuePractice(userDetails.getUsername(), msg);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/tournament/create")
    @ApiOperation(value = "${QueueController.tournament_create}")
    @PreAuthorize("hasRole('ROLE_OFFICIAL')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public ResponseEntity<String> createTournament(@AuthenticationPrincipal UserDetails userDetails,
                                                   @ApiParam("Create tournament msg") @RequestBody RequestCreateTournamentMessage msg)
            throws InterruptedException, ExecutionException, TimeoutException {

        playService.createTournament(userDetails.getUsername(), msg);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/tournament/join")
    @ApiOperation(value = "${QueueController.tournament}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public ResponseEntity<String> joinTournament(@AuthenticationPrincipal UserDetails userDetails,
                                                 @ApiParam("Join tournament msg") @RequestBody String tournamentID)
            throws InterruptedException, ExecutionException, TimeoutException {

        playService.joinTournament(userDetails.getUsername(), tournamentID);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


    @GetMapping("/play/state/{playID}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public PlayStateResponseDTO getStateOfPlay(@ApiParam("playID") @PathVariable String playID) {
        final ReadOnlyKeyValueStore<String, PlayMessage> songStore =
                interactiveQueryService.getQueryableStore("plays", QueryableStoreTypes.keyValueStore());
        final PlayMessage state = songStore.get(playID);
        if (state == null) {
            throw new IllegalArgumentException("...");
        }
        return modelMapper.map(state, PlayStateResponseDTO.class);
    }
}
