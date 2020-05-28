package pm.controller;

import com.google.gson.Gson;
import exception.CustomException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import message.created.PlayMessage;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@RestController
@RequestMapping("")
public class PMController {
    private static final Logger logger = LoggerFactory.getLogger(PMController.class);
    private static final Gson gson = new Gson();
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
    private final RestTemplate restTemplate = new RestTemplate();
    private final String playStateStoreName = "play-state-store";
    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @ExceptionHandler({Exception.class, CustomException.class})
    public ResponseEntity<String> handleException(Exception ex, WebRequest request) {
        logger.error(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @GetMapping(value = "/ping/{value}")
    @ApiResponses(value = @ApiResponse(code = 400, message = "Something went wrong"))
    public String ping(@PathVariable String value) {
        return String.format("Payload [%s]", value);
    }


    @PostMapping(value = "/play")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 500, message = "Null keys aren't allowed!")})
    public String retrievePlay(@RequestHeader(value = "Authorization") String token,   //Request prefixes are for web
                               @RequestBody String playID) {
        //Host of the key
        HostInfo hostInfo = interactiveQueryService.getHostInfo(playStateStoreName, playID, new StringSerializer());

        //Retrieve play from state store
        PlayMessage play;
        if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
            //Retrieve from local state store
            final ReadOnlyKeyValueStore<String, PlayMessage> store =
                    interactiveQueryService.getQueryableStore(playStateStoreName, QueryableStoreTypes.keyValueStore());
            play = store.get(playID);
            logger.info(String.format("retrievePlay: Retrieved local Play [%s] from [%s].", play == null ? null : play.toString(), hostInfo.toString()));
        } else {
            //Retrieve from remote state store
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            headers.setCacheControl(CacheControl.noCache());
            headers.setBearerAuth(token.split(" ")[1]);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("http://%s:%d/play/%s", hostInfo.host(), hostInfo.port(), playID));
            play = restTemplate.postForEntity(builder.toUriString(), headers, PlayMessage.class).getBody();
            logger.info(String.format("retrievePlay: Retrieved remote Play [%s] from [%s].", play == null ? null : play.toString(), hostInfo.toString()));
        }

        return gson.toJson(play);
    }

}
