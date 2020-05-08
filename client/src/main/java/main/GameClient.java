package main;

import game.GameType;
import game.PlayType;
import message.JoinPlayMessage;
import org.apache.commons.cli.*;
import org.springframework.http.HttpEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import websocket.Message;
import websocket.MyStompSessionHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class GameClient {
    private static final String SIGNIN_URL = "http://localhost:8080/users/signin";
    private static final String SIGNUP_URL = "http://localhost:8080/users/signup";
    private static final String PRACTICE_URL = "http://localhost:8080/queue/practice/";
    private static final String SEARCH_URL = "http://localhost:8080/users/";
    private static Random random = new Random();

    public static void main(String[] args) throws FileNotFoundException {
        //Parse input
        CommandLine cmd = parseInput(args);
        if (cmd == null) {
            System.out.println("Error parsing arguments.");
            return;
        }

        //Check if we need to read from file
        if (cmd.hasOption("fromFile")) {
            System.setIn(new FileInputStream(cmd.getOptionValue("fromFile")));
        }

        //For user input
        Scanner scanner = new Scanner(System.in);
        System.exit(handleInputs(scanner));
    }

    private static int handleInputs(Scanner scanner) {
        Map<Integer, String> options = new HashMap<>();
        options.put(1, "Sign-Up");  //OK
        options.put(2, "Sign in");  //OK
        options.put(3, "Practice Play");    //TODO in-progress
        options.put(4, "Create Tournament (OFFICIAL ONLY)");
        options.put(5, "Join Tournament");
        options.put(6, "Spectate Game");
        options.put(7, "Search user (ADMIN ONLY)"); //OK
        options.put(8, "My stats");
        options.put(9, "Exit"); //OK

        //REST client
        RESTClient client = new RESTClient();

        //User token
        String token = "";
        String username = "";

        while (true) {
            try {
                //Check if we need to exit
                if (!scanner.hasNext()) {
                    break;
                }

                //Print the menu
                System.out.println("\n\n*** Menu ***");
                for (Map.Entry<Integer, String> entry : options.entrySet()) {
                    System.out.println(String.format("%d. %s", entry.getKey(), entry.getValue()));
                }

                //Chose an option
                System.out.print("Answer: ");
                int choice = Integer.parseInt(scanner.next());
                System.out.println(choice);

                //Perform an action
                switch (choice) {
                    //Sign up
                    case 1:
                        //Arguments
                        System.out.print("Enter username, password, email and role (separated with spaces): ");
                        Map<String, String> signupParams = new LinkedHashMap<>();
                        signupParams.put("username", scanner.next());
                        username = signupParams.get("username");
                        signupParams.put("password", scanner.next());
                        signupParams.put("email", scanner.next());
                        signupParams.put("role", scanner.next());
                        System.out.println(signupParams);

                        //Response = executed entity
                        HttpEntity<String> signUpResponse;
                        try {
                            signUpResponse = client.signup(SIGNUP_URL, signupParams);
                        } catch (HttpClientErrorException.UnprocessableEntity e1) {
                            System.out.println(e1.getMessage());
                            break;
                        }
                        token = signUpResponse.getBody();
                        System.out.println("Token: " + token);
                        break;

                    //Sign In
                    case 2:
                        //Arguments
                        System.out.print("Enter username and password: ");
                        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
                        loginParams.add("username", scanner.next());
                        loginParams.add("password", scanner.next());
                        System.out.println(loginParams);

                        HttpEntity<String> loginResponse;
                        try {
                            loginResponse = client.login(SIGNIN_URL, loginParams);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            break;
                        }

                        //Token
                        token = loginResponse.getBody();
                        System.out.println("Token: " + token);
                        break;

                    case 3:
                        //Arguments
                        System.out.print("Queueing up for a practice play. Enter preferred game type: ");
                        GameType gameType = GameType.valueOf(scanner.next());

                        //Queue up
                        HttpEntity<String> practiceResponse;
                        try {
                            practiceResponse = client.practice(PRACTICE_URL, token, gameType);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            break;
                        }
                        System.out.println(practiceResponse.getBody());

                        if (1==1){
                            System.exit(9);
                        }

                        //Start
                        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
                        List<String> subs = new ArrayList<>();
                        subs.add("/topic/messages");
                        StompSession stompSession;
                        try {
                            stompSession = stompClient.connect("ws://localhost:8080/chat", new MyStompSessionHandler(subs)).get();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            break;
                        }
                        int seed = random.nextInt(100000);
                        stompSession.send("/app/chat", new Message("User" + seed, "payload" + seed));
                        break;

                    case 7:
                        //Arguments
                        System.out.print("Search username: ");
                        String usernameToSearch = scanner.next();
                        System.out.println(usernameToSearch);

                        HttpEntity<String> searchResponse;
                        try {
                            searchResponse = client.search(SEARCH_URL, usernameToSearch, token);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            break;
                        }

                        //Token
                        String searchResult = searchResponse.getBody();
                        System.out.println("Result: " + searchResult);
                        break;

                    //Exit
                    case 9:
                        System.out.println("Exiting..");
                        break;

                    //Handle errors
                    default:
                        System.out.println("Invalid option");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static CommandLine parseInput(String[] args) {
        Options options = new Options();

        Option output = new Option("f", "fromFile", true, "Get user input from a resource file instead of STDIN.");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            return null;
        }
        return cmd;
    }

}

/*

    HttpHeaders headers = new HttpHeaders();
                        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                                HttpEntity<String> entity = new HttpEntity<>("body", headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        Map<String, String> arguments = new HashMap<>();
        arguments.put("username", scanner.next());
        arguments.put("password", scanner.next());
        ResponseEntity<String> response = restTemplate.getForEntity(
        "http://127.0.0.1:8080/users/signin?username={username}&password={password}",
        String.class,
        arguments);

 */