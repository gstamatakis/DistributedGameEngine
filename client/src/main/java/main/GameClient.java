package main;

import com.google.common.collect.Queues;
import game.GameType;
import message.requests.RequestCreateTournamentMessage;
import org.apache.commons.cli.*;
import org.springframework.http.HttpEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import websocket.InputSTOMPMessage;
import websocket.MyStompSessionHandler;
import websocket.OutputSTOMPMessage;

import java.io.*;
import java.util.*;

public class GameClient {
    private static final String SIGN_IN_URL = "http://localhost:8080/users/signin";
    private static final String SIGNUP_URL = "http://localhost:8080/users/signup";
    private static final String PRACTICE_URL = "http://localhost:8080/queue/practice";
    private static final String TOURNAMENT_CREATE_URL = "http://localhost:8080/queue/tournament/create";
    private static final String TOURNAMENT_JOIN_URL = "http://localhost:8080/queue/tournament/join";
    private static final String SEARCH_URL = "http://localhost:8080/users/";
    private static final String STOMP_CONNECT_URL = "ws://localhost:8080/play";

    public static void main(String[] args) throws Exception {
        //Parse input
        CommandLine cmd = parseInput(args);
        if (cmd == null) {
            System.out.println("Error parsing arguments.");
            return;
        }

        //Check if we need to read from file
        Scanner scanner;
        if (cmd.hasOption("fromFile")) {
            scanner = new Scanner(new FileInputStream(cmd.getOptionValue("fromFile")));
        } else {
            scanner = new Scanner(System.in);
        }

        //Destination stream
        OutputStream outputStream = System.out;
        if (cmd.hasOption("silent")) {
            if (cmd.getOptionValue("silent").equals("true")) {
                outputStream = new NullPrintStream();
            }
        }

        //Output writer
        OutputStreamWriter output = new OutputStreamWriter(new BufferedOutputStream(outputStream));

        //Execute the main loop
        int result = handleInputs(scanner, output);
        output.write("\nExit code: " + result + "\n");
        output.flush();
    }

    public static int handleInputs(Scanner scanner, OutputStreamWriter output) throws Exception {
        Map<Integer, String> options = new HashMap<>();
        options.put(1, "Sign-Up");  //OK
        options.put(2, "Sign in");  //OK
        options.put(3, "Practice Play");    //TODO in-progress
        options.put(4, "Join Tournament");
        options.put(5, "Create Tournament (OFFICIAL ONLY)");
        options.put(6, "Rejoin game");
        options.put(7, "Spectate Game");
        options.put(8, "Search user (ADMIN ONLY)"); //OK
        options.put(9, "My stats");
        options.put(10, "Exit"); //OK

        //REST client
        RESTClient client = new RESTClient();

        //User token
        String token = "";
        String username = "";

        while (scanner.hasNext()) {
            //Check if we need to exit

            //Print the menu
            output.write("\n\n*** Menu ***");
            for (Map.Entry<Integer, String> entry : options.entrySet()) {
                output.write(String.format("\n%d. %s", entry.getKey(), entry.getValue()));
            }

            //Chose an option
            output.write("\nAnswer: ");
            int choice = Integer.parseInt(scanner.next());
            output.write(String.valueOf(choice));

            //Perform an action
            switch (choice) {
                //Sign up
                case 1:
                    //Arguments
                    output.write("\nEnter username, password, email and role (separated with spaces): ");
                    Map<String, String> signupParams = new LinkedHashMap<>();
                    signupParams.put("username", scanner.next());
                    username = signupParams.get("username");
                    signupParams.put("password", scanner.next());
                    signupParams.put("email", scanner.next());
                    signupParams.put("role", scanner.next());
                    output.write("\n" + signupParams.toString());

                    //Response = executed entity
                    HttpEntity<String> signUpResponse;
                    try {
                        signUpResponse = client.signup(SIGNUP_URL, signupParams);
                    } catch (HttpClientErrorException.UnprocessableEntity e1) {
                        output.write("\n" + e1.getMessage());
                        break;
                    }
                    token = signUpResponse.getBody();
                    output.write("\nToken: " + token);
                    break;

                //Sign In
                case 2:
                    //Arguments
                    output.write("\nEnter username and password: ");
                    MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();
                    loginParams.add("username", scanner.next());
                    loginParams.add("password", scanner.next());
                    output.write("\n" + loginParams);

                    HttpEntity<String> loginResponse;
                    try {
                        loginResponse = client.login(SIGN_IN_URL, loginParams);
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        break;
                    }

                    //Token
                    token = loginResponse.getBody();
                    output.write("\nToken: " + token);
                    break;

                case 3:
                    //Arguments
                    output.write("\nQueueing up for a practice play. Enter preferred game type: ");
                    GameType gameType = GameType.valueOf(scanner.next());
                    output.write("\n" + gameType.toString());

                    //Queue up
                    HttpEntity<String> practiceResponse;
                    try {
                        practiceResponse = client.practice(PRACTICE_URL, token, gameType, username);
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        break;
                    }
                    output.write("\nPractice play enqueued status: " + practiceResponse.getBody());

                    //Start
                    WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                    stompClient.setMessageConverter(new MappingJackson2MessageConverter());
                    StompSession stompSession;
                    Queue<OutputSTOMPMessage> queue = Queues.newArrayBlockingQueue(10);
                    try {
                        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
                        webSocketHttpHeaders.add("Authorization", "Bearer " + token);

                        List<String> subs = new ArrayList<>();
                        subs.add("/user/queue/reply");
                        subs.add("/user/queue/errors");

                        stompSession = stompClient.connect(STOMP_CONNECT_URL, webSocketHttpHeaders, new MyStompSessionHandler(subs,queue)).get();
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        break;
                    }

                    stompSession.send("/app/play", new InputSTOMPMessage(username, "payload_" + username));
                    break;

                case 4:
                    //Arguments
                    output.write("\nEnter the tournament ID to join a tournament: ");
                    String tournamentID = scanner.next();
                    output.write("\n" + tournamentID);

                    //Queue up
                    HttpEntity<String> tournamentJoinResponse;
                    try {
                        tournamentJoinResponse = client.joinTournament(TOURNAMENT_JOIN_URL, token, tournamentID);
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        break;
                    }
                    output.write("\nTournament play enqueued status: " + tournamentJoinResponse.getBody());
                    break;

                case 5:
                    output.write("\nCreating a tournament play. Enter preferred game type,num of participants and tournament ID: ");
                    GameType tournamentGameType = GameType.valueOf(scanner.next());
                    int numOfParticipants = Integer.parseInt(scanner.next());
                    String newTournamentID = scanner.next();
                    Set<String> blackList = new HashSet<>();
                    RequestCreateTournamentMessage msg = new RequestCreateTournamentMessage(tournamentGameType, blackList, numOfParticipants, newTournamentID);
                    output.write("\n" + msg.toString());

                    //Queue up
                    HttpEntity<String> tournamentResponse;
                    try {
                        tournamentResponse = client.createTournament(TOURNAMENT_CREATE_URL, token, msg);
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        break;
                    }
                    output.write("\nTournament play created status: " + tournamentResponse.getBody());
                    break;

                case 7:
                    //Arguments
                    output.write("\nSearch username: ");
                    String usernameToSearch = scanner.next();
                    output.write("\n" + usernameToSearch);

                    HttpEntity<String> searchResponse;
                    try {
                        searchResponse = client.search(SEARCH_URL, usernameToSearch, token);
                    } catch (Exception e) {
                        output.write("\n" + e.getMessage());
                        return -1;
                    }

                    //Token
                    String searchResult = searchResponse.getBody();
                    output.write("\nResult: " + searchResult);
                    break;

                //Exit
                case 10:
                    output.write("\n\nExiting..");
                    break;

                //Handle errors
                default:
                    throw new IllegalStateException("Invalid option");
            }
        }
        return 0;
    }

    private static CommandLine parseInput(String[] args) {
        Options options = new Options();

        Option output = new Option("f", "fromFile", true, "Get user input from a resource file instead of STDIN.");
        output.setRequired(true);
        options.addOption(output);

        Option silent = new Option("s", "silent", true, "Redirect everything to STDOUT.");
        silent.setRequired(false);
        options.addOption(silent);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("User client", options);
            return null;
        }
    }


    public static class NullPrintStream extends PrintStream {

        public NullPrintStream() {
            super(new NullByteArrayOutputStream());
        }

        private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

            @Override
            public void write(int b) {
                // do nothing
            }

            @Override
            public void write(byte[] b, int off, int len) {
                // do nothing
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                // do nothing
            }

        }
    }
}