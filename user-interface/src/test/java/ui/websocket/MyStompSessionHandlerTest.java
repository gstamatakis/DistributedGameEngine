package ui.websocket;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.util.UriComponentsBuilder;
import websocket.MyStompSessionHandler;
import websocket.DefaultSTOMPMessage;
import websocket.STOMPMessageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MyStompSessionHandlerTest {
    private static Random random = new Random();
    private static ExecutorService pool;
    private static RestTemplate restTemplate = new RestTemplate();
    private static final String SIGN_IN_URL = "http://localhost:8080/users/signin";
    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandlerTest.class);

    @BeforeAll
    static void setUp() {
        pool = new ForkJoinPool(4);
    }

    @AfterEach
    void afterEach() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    //Get a valid token from the UI service
    private synchronized static String login(String username, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        params.add("password", password);

        //Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.setCacheControl(CacheControl.noCache());

        //Arguments
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SIGN_IN_URL).queryParams(params);

        //Entity = Headers + Arguments
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        //Response = executed entity
        return restTemplate.postForEntity(builder.toUriString(), entity, String.class).getBody();
    }

    @Test
    @Order(1)
    public void echo_test() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession stompSession = null;
        String token = login("admin", "admin");
        ArrayBlockingQueue<DefaultSTOMPMessage> queue = new ArrayBlockingQueue<>(10);
        try {
            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
            webSocketHttpHeaders.add("Authorization", "Bearer " + token);

            List<String> subs = new ArrayList<>();
            subs.add("/user/queue/reply");
            subs.add("/user/queue/errors");
            subs.add("/topic/broadcast");
            stompSession = stompClient.connect("ws://localhost:8080/play", webSocketHttpHeaders, new MyStompSessionHandler(subs, queue)).get();
        } catch (Exception e) {
            Assertions.fail(e);
        }

        for (int i = 0; i < 10; i++) {
            //Send something
            int seed = random.nextInt(100000);
            DefaultSTOMPMessage newInputMsg = new DefaultSTOMPMessage("", String.format("payload%06d", seed), STOMPMessageType.NOTIFICATION, null, null);
            stompSession.send("/app/echo", newInputMsg);

            //Get an echo response
            try {
                DefaultSTOMPMessage newOutputMsg = queue.poll(5, TimeUnit.SECONDS);
                if (newOutputMsg == null) {
                    break;
                }
                Assertions.assertEquals(newInputMsg.getPayload(), newOutputMsg.getPayload());
                //Wait before sending the next message
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Test
    @Order(2)
    @Disabled
    public void concurrent_echo_test() {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(pool.submit(this::echo_test));
        }
        int cnt = 1;
        for (Future<?> future : futures) {
            try {
                future.get();
                logger.info(String.format("Future %d completed.", cnt++));
            } catch (Exception e) {
                Assertions.fail(e);
            }
        }
    }
}