package ui.websocket;

import org.junit.jupiter.api.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import websocket.Message;
import websocket.MyStompSessionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

class MyStompSessionHandlerTest {
    private static Random random = new Random();
    private static ExecutorService pool;

    @BeforeAll
    static void setUp() {
        pool = new ForkJoinPool(4);
    }

    @AfterEach
    void afterEach() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
    }

    @AfterAll
    static void tearDown() {
        pool.shutdown();
    }

    @Test
    public void echo_test() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        List<String> subs = new ArrayList<>();
        subs.add("/user/queue/errors");
        subs.add("/user/queue/reply");
        StompSession stompSession = null;
        try {
            stompSession = stompClient.connect("ws://localhost:8080/echo", new MyStompSessionHandler(subs)).get();
        } catch (Exception e) {
            Assertions.fail(e);
        }
        int seed = random.nextInt(100000);
        stompSession.send("/app/echo", new Message("User" + seed, "payload" + seed));
    }

    @Test
    public void chat_test() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        List<String> subs = new ArrayList<>();
        subs.add("/topic/messages");
        StompSession stompSession = null;
        try {
            stompSession = stompClient.connect("ws://localhost:8080/chat", new MyStompSessionHandler(subs)).get();
        } catch (Exception e) {
            Assertions.fail(e);
        }
        int seed = random.nextInt(100000);
        stompSession.send("/app/chat", new Message("User" + seed, "payload" + seed));
    }

    @Test
    public void multiple_chat_test() {
        pool.submit(this::chat_test);
        pool.submit(this::chat_test);
        pool.submit(this::chat_test);
        pool.submit(this::chat_test);

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
}