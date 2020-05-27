package ui.controller;

import com.google.gson.Gson;
import dto.UserDataDTO;
import dto.UserResponseDTO;
import exception.CustomException;
import io.swagger.annotations.*;
import message.completed.UserScore;
import message.created.PlayMessage;
import model.PlayTypeEnum;
import model.Role;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import ui.model.UserEntity;
import ui.service.PlayService;
import ui.service.UserService;
import websocket.DefaultSTOMPMessage;
import websocket.STOMPMessageType;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/users")
@Api(tags = "users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Value(value = "${gamemaster.store.url}")
    private String gameMasterURL;

    @Autowired
    private PlayService playService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<String> handleConflict(CustomException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @PostMapping("/signin")
    @ApiOperation(value = "${UserController.signin}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public String login(
            @ApiParam("Username") @RequestParam String username,
            @ApiParam("Password") @RequestParam String password) {
        return userService.signin(username, password);
    }

    @PostMapping("/signup")
    @ApiOperation(value = "${UserController.signup}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Username is already in use"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String signup(@ApiParam("Signup User") @RequestBody UserDataDTO user) {
        return userService.signup(modelMapper.map(user, UserEntity.class));
    }

    @PostMapping(value = "/createofficial")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 422, message = "Username is already in use"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String createOfficial(@RequestBody UserDataDTO user) {
        return userService.signup(modelMapper.map(user, UserEntity.class), Role.ROLE_OFFICIAL);
    }

    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.delete}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String delete(@ApiParam("Username") @PathVariable String username) {
        userService.delete(username);
        return username;
    }

    @GetMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.search}", response = UserResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserResponseDTO search(@ApiParam("Username") @PathVariable String username) {
        return modelMapper.map(userService.search(username), UserResponseDTO.class);
    }

    @PostMapping(value = "/score/{username}")
    @ApiOperation(value = "${UserController.score}", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String retrieveScore(@RequestHeader(value = "PlayType") String playType,
                                @RequestHeader(value = "Authorization") String token,
                                @PathVariable String username,
                                Principal principal) {

        logger.info(String.format("retrieveScore: Received message from [%s] to search [%s].", principal.getName(), username));
        PlayTypeEnum playTypeEnum = PlayTypeEnum.valueOf(playType);
        String destURL;
        switch (playTypeEnum) {
            case PRACTICE:
                destURL = gameMasterURL + "/score/practice/";
                break;
            case TOURNAMENT:
                destURL = gameMasterURL + "/score/tournament/";
                break;
            default:
                throw new IllegalStateException(String.format("retrieveScore: Invalid play type supplied [%s]", playType));
        }

        //Message setup
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.split(" ")[1]);
        HttpEntity<String> entity = new HttpEntity<>(username, headers);

        //Retrieve from PlayMaster service
        String scoreJson = restTemplate.postForEntity(destURL, entity, String.class).getBody();
        UserScore userScore = gson.fromJson(scoreJson, UserScore.class);
        logger.info(String.format("retrieveScore: Received score from %s [%s].", username, userScore == null ? null : userScore.toString()));

        //Send it back to user
        if (userScore == null) {
            return (new UserScore(principal.getName())).getScoreString(playTypeEnum, principal.getName().equals(username));
        }
        return userScore.getScoreString(playTypeEnum, principal.getName().equals(username));
    }

    @PostMapping(value = "/play")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String retrievePlay(@RequestHeader(value = "Authorization") String token,
                               Principal principal) {

        String username = principal.getName();
        logger.info(String.format("retrievePlay: Received message from [%s].", username));

        //Message setup
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.split(" ")[1]);
        HttpEntity<String> entity = new HttpEntity<>(username, headers);

        //Retrieve from PlayMaster service
        String scoreJson = restTemplate.postForEntity(gameMasterURL + "/play", entity, String.class).getBody();
        PlayMessage playMessage = gson.fromJson(scoreJson, PlayMessage.class);
        if (playMessage == null) {
            return "null";
        }

        //Send messages that inform users that needs to move/wait
        messagingTemplate.convertAndSendToUser(playMessage.getNeedsToMove(), "/queue/reply",
                new DefaultSTOMPMessage(
                        playMessage.getNeedsToMove(),
                        String.format("You need to make a move [%s].", playMessage.getNeedsToMove()),
                        STOMPMessageType.NEED_TO_MOVE,
                        null,
                        playMessage.getID()));

        messagingTemplate.convertAndSendToUser(playMessage.getNeedsToWait(), "/queue/reply",
                new DefaultSTOMPMessage(
                        playMessage.getNeedsToWait(),
                        String.format("Waiting for [%s] to make a move.", playMessage.getNeedsToMove()),
                        STOMPMessageType.NOTIFICATION,
                        null,
                        playMessage.getID()));

        logger.info(String.format("retrievePlay: Retrieved user [%s] with play [%s].", username, playMessage.toString()));
        return gson.toJson(playMessage);
    }

    @GetMapping(value = "/me")
    @ApiOperation(value = "${UserController.me}", response = UserResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserResponseDTO whoami(HttpServletRequest req) {
        return modelMapper.map(userService.whoami(req), UserResponseDTO.class);
    }

    @GetMapping(value = "/spectate/{playid}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Boolean registerSpectate(@PathVariable String playid, Principal principal) {
        return playService.registerSpectator(principal.getName(), playid);
    }

    @GetMapping("/refresh")
    public String refresh(HttpServletRequest req) {
        return userService.refresh(req.getRemoteUser());
    }

}
