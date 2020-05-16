package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.GameTypeEnum;
import message.PlayTypeMessage;
import message.requests.RequestCreateTournamentMessage;
import message.requests.RequestPracticeMessage;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

public class RESTClient {
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public RESTClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ResponseEntity<String> login(String url, MultiValueMap<String, String> params) {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);

        //Arguments
        if (params == null) {
            params = new LinkedMultiValueMap<>();
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParams(params);

        //Entity = Headers + Arguments
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        //Response = executed entity
        return restTemplate.postForEntity(builder.toUriString(), entity, String.class);
    }

    public ResponseEntity<String> signup(String url, Map<String, String> signupParams) throws JsonProcessingException {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);

        //Convert input to JSON
        String json = this.objectMapper.writeValueAsString(signupParams);

        //Entity = Headers + Arguments
        HttpEntity<Object> entity = new HttpEntity<>(json, headers);

        //Response = executed entity
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public HttpEntity<String> practice(String practiceURL, String token, GameTypeEnum gameType, String username) {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);
        headers.setBearerAuth(token);

        PlayTypeMessage msg = new RequestPracticeMessage(username, gameType);

        //Entity = Headers + Arguments
        HttpEntity<Object> entity = new HttpEntity<>(msg, headers);

        //Response = executed entity
        return restTemplate.exchange(practiceURL, HttpMethod.POST, entity, String.class);
    }

    public HttpEntity<String> search(String searchUrl, String usernameToSearch, String token) {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);
        headers.setBearerAuth(token);

        //Entity = Headers + Arguments
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        //Response = executed entity
        return restTemplate.exchange(searchUrl + "/" + usernameToSearch, HttpMethod.GET, entity, String.class);
    }

    public HttpEntity<String> createTournament(String tournamentCreateUrl, String token, RequestCreateTournamentMessage message) {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);
        headers.setBearerAuth(token);

        //Entity = Headers + Argument
        HttpEntity<Object> entity = new HttpEntity<>(message, headers);

        //Response = executed entity
        return restTemplate.exchange(tournamentCreateUrl, HttpMethod.POST, entity, String.class);
    }

    public HttpEntity<String> joinTournament(String tournamentJoinUrl, String token, String tournamentID) {
        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache());
        headers.add("user-agent", USER_AGENT);
        headers.setBearerAuth(token);

        //Entity = Headers + Argument
        HttpEntity<Object> entity = new HttpEntity<>(tournamentID, headers);

        //Response = executed entity
        return restTemplate.exchange(tournamentJoinUrl, HttpMethod.POST, entity, String.class);
    }
}
