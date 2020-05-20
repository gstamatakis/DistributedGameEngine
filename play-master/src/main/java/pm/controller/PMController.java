package pm.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@RestController
@RequestMapping("")
public class PMController {
    private static final Logger logger = LoggerFactory.getLogger(PMController.class);

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @Value(value = "${token.service}")
    private String serviceToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String playStateStoreName = "play-state-store";

    @GetMapping(value = "/ping/{value}")
    @ApiResponses(value = @ApiResponse(code = 400, message = "Something went wrong"))
    public String ping(@PathVariable String value) {
        return value;
    }


    @PostMapping(value = "/play/{playID}")
    @ApiResponses(value = @ApiResponse(code = 400, message = "Something went wrong"))
    public PlayMessage retrievePlay(@PathVariable String playID) {
        //Host of the key
        HostInfo hostInfo = interactiveQueryService.getHostInfo(playStateStoreName, playID, new StringSerializer());

        //Retrieve play from state store
        PlayMessage play;
        if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
            //Retrieve from local state store
            final ReadOnlyKeyValueStore<String, PlayMessage> store =
                    interactiveQueryService.getQueryableStore(playStateStoreName, QueryableStoreTypes.keyValueStore());
            play = store.get(playID);
        } else {
            //Retrieve from remote state store
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            headers.setCacheControl(CacheControl.noCache());
            headers.setBearerAuth(serviceToken);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(String.format("http://%s:%d/play/%s", hostInfo.host(), hostInfo.port(), playID));
            play = restTemplate.postForEntity(builder.toUriString(), headers, PlayMessage.class).getBody();
        }

        logger.info(String.format("Retrieved Play [%s] from [%s].", play, hostInfo.toString()));
        return play;
    }

}
