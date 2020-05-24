package ui.controller;

import com.google.gson.Gson;
import dto.UserDataDTO;
import dto.UserResponseDTO;
import exception.CustomException;
import io.swagger.annotations.*;
import message.completed.UserScore;
import model.PlayTypeEnum;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import ui.model.UserEntity;
import ui.service.UserService;

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

    @Value(value = "${playmaster.store.url}")
    private String playMasterURL;


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
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiOperation(value = "${UserController.score}", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "The user doesn't exist"),
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String retrieveScore(@RequestHeader(value = "PlayType") String playType,
                                @RequestHeader(value = "Authorization") String token,
                                @PathVariable String username,
                                Principal principal) {

        logger.info(String.format("Received message in retrieveScore from %s [%s].", principal.getName(), username));
        PlayTypeEnum playTypeEnum = PlayTypeEnum.valueOf(playType);
        String destURL;
        switch (playTypeEnum) {
            case PRACTICE:
                destURL = playMasterURL + "/score/practice/";
                break;
            case TOURNAMENT:
                destURL = playMasterURL + "/score/tournament/";
                break;
            default:
                throw new IllegalStateException(String.format("Invalid play type supplied [%s]", playType));
        }

        //Message setup
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(username, headers);

        //Retrieve from PlayMaster service
        String scoreJson = restTemplate.postForEntity(destURL, entity, String.class).getBody();
        UserScore userScore = gson.fromJson(scoreJson, UserScore.class);
        logger.info(String.format("Received score in retrieveScore from %s [%s].", username, userScore.toString()));

        //Send it back to user
        return userScore.getScoreString(playTypeEnum, principal.getName().equals(username));
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

    @GetMapping("/refresh")
    public String refresh(HttpServletRequest req) {
        return userService.refresh(req.getRemoteUser());
    }

}
