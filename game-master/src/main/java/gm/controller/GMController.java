package gm.controller;

import com.google.gson.Gson;
import exception.CustomException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import message.completed.UserScore;
import message.created.PlayMessage;
import model.PlayTypeEnum;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@RestController
@RequestMapping("")
public class GMController {
    private static final Logger logger = LoggerFactory.getLogger(GMController.class);
    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();

    private final String practiceScoreStore = "practice-score-store";
    private final String tournamentScoreStore = "tournament-score-store";
    private final String userToPlayIDStore = "user-to-playID";

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @ExceptionHandler({Exception.class, CustomException.class})
    public ResponseEntity<String> handleException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @GetMapping(value = "/test/{value}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    @ApiOperation(value = "${UserController.test}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String ping(@PathVariable String value) {
        return "Sent " + value;
    }

    @PostMapping(value = "/score/{playType}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String retrievePracticeScore(@PathVariable String playType,
                                        @RequestHeader(value = "Authorization") String token,   //Request prefixes are for web
                                        @RequestBody String username) {
        //Host of the key
        logger.info(String.format("retrievePracticeScore: Looking for [%s],[%s]", username, playType));
        String playStateStoreName = playType.equalsIgnoreCase(PlayTypeEnum.PRACTICE.name()) ? practiceScoreStore : tournamentScoreStore;
        HostInfo hostInfo = interactiveQueryService.getHostInfo(playStateStoreName, username, new StringSerializer());

        //Retrieve play from state store
        UserScore userScore;
        if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
            //Retrieve from local state store
            final ReadOnlyKeyValueStore<String, UserScore> store =
                    interactiveQueryService.getQueryableStore(playStateStoreName, QueryableStoreTypes.keyValueStore());
            userScore = store.get(username);
        } else {
            //Retrieve from remote state store
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            headers.setCacheControl(CacheControl.noCache());
            headers.setBearerAuth(token.split(" ")[1]);
            HttpEntity<String> entity = new HttpEntity<>(username, headers);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("http://%s:%d/score/%s", hostInfo.host(), hostInfo.port(), playType));
            userScore = restTemplate.postForEntity(builder.toUriString(), entity, UserScore.class).getBody();
        }

        logger.info(String.format("Retrieved UserScore [%s] from [%s].", userScore, hostInfo.toString()));
        return gson.toJson(userScore);
    }

    @PostMapping(value = "/play")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String retrievePlay(@RequestBody String username,
                               @RequestHeader(value = "Authorization") String token) { //Request prefixes are for web
        //Host of the Play message
        logger.info(String.format("retrievePlay: Looking for a play for [%s]", username));
        HostInfo hostInfo = interactiveQueryService.getHostInfo(userToPlayIDStore, username, new StringSerializer());

        //Retrieve play from state store
        PlayMessage playMessage;
        if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
            //Retrieve from local state store
            final ReadOnlyKeyValueStore<String, PlayMessage> store =
                    interactiveQueryService.getQueryableStore(userToPlayIDStore, QueryableStoreTypes.keyValueStore());
            playMessage = store.get(username);
        } else {
            //Retrieve from remote state store
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            headers.setCacheControl(CacheControl.noCache());
            headers.setBearerAuth(token.split(" ")[1]);
            HttpEntity<String> entity = new HttpEntity<>(username, headers);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("http://%s:%d/play", hostInfo.host(), hostInfo.port()));
            String remotePlayJson = restTemplate.postForEntity(builder.toUriString(), entity, String.class).getBody();
            playMessage = gson.fromJson(remotePlayJson, PlayMessage.class);
        }

        logger.info(String.format("retrievePlay: Retrieved PlayMessage [%s] from [%s].", playMessage, hostInfo.toString()));
        return gson.toJson(playMessage);
    }
}
