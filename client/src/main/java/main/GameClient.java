package main;

import message.requests.RequestCreateTournamentMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;
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
import websocket.DefaultSTOMPMessage;
import websocket.MyStompSessionHandler;
import websocket.STOMPMessageType;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameClient {
    private static final String SIGN_IN_URL = "http://localhost:8080/users/signin";
    private static final String SIGNUP_URL = "http://localhost:8080/users/signup";
    private static final String PRACTICE_URL = "http://localhost:8080/queue/practice";
    private static final String SCORE_URL = "http://localhost:8080/user/score";
    private static final String TOURNAMENT_CREATE_URL = "http://localhost:8080/queue/tournament/create";
    private static final String TOURNAMENT_JOIN_URL = "http://localhost:8080/queue/tournament/join";
    private static final String SEARCH_URL = "http://localhost:8080/users/";
    private static final String STOMP_CONNECT_URL = "ws://localhost:8080/play";

    private static boolean ctrlC = false;

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
        BufferedWriter output;
        if (cmd.hasOption("silent")) {
            output = new BufferedWriter(new OutputStreamWriter(new NullPrintStream()));
        } else {
            output = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        //Handle Ctrl+C
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> ctrlC = true));

        //Execute the main loop
        int result = handleInputs(scanner, output);
        output.write("\nExit code: " + result + "\n");
        output.close();
    }

    public static int handleInputs(Scanner scanner, BufferedWriter output) throws Exception {
        Map<Integer, String> options = new HashMap<>();
        options.put(1, "Sign Up");  //OK
        options.put(2, "Sign in");  //OK
        options.put(3, "Queue up for a Practice Play");
        options.put(4, "Join a Tournament");
        options.put(5, "Create a Tournament (OFFICIAL ONLY)");
        options.put(6, "(Re)Join game");
        options.put(7, "Spectate Game");
        options.put(8, "User Stats");
        options.put(9, "Search user (ADMIN ONLY)");
        options.put(10, "Exit"); //OK

        //REST client
        RESTClient client = new RESTClient();

        //User token
        String token = "";
        String username = "";
        WebSocketStompClient stompClient;
        StompSession stompSession = null;
        ArrayBlockingQueue<DefaultSTOMPMessage> queue = new ArrayBlockingQueue<>(10);

        while (true) {
            try {

                //Print the menu
                output.write("\n\n*** Menu ***");
                for (Map.Entry<Integer, String> entry : options.entrySet()) {
                    output.write(String.format("\n%d. %s", entry.getKey(), entry.getValue()));
                }

                //Chose an option
                output.write("\nAnswer: ");
                output.flush();
                int choice = scanner.nextInt();
                output.write("\nAnswered: " + choice);
                output.flush();

                //Perform an action
                switch (choice) {
                    //Sign up
                    case 1:
                        //Arguments
                        Map<String, String> signupParams = new LinkedHashMap<>();
                        output.write("\nEnter username: ");
                        output.flush();
                        signupParams.put("username", scanner.next());
                        username = signupParams.get("username");

                        output.write("\nEnter password: ");
                        output.flush();
                        signupParams.put("password", scanner.next());

                        output.write("\nEnter email: ");
                        output.flush();
                        signupParams.put("email", scanner.next());

                        output.write("\nEnter role: ");
                        output.flush();
                        signupParams.put("role", scanner.next());

                        output.write("\n" + signupParams.toString());
                        output.flush();

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
                        MultiValueMap<String, String> loginParams = new LinkedMultiValueMap<>();

                        output.write("\nEnter username: ");
                        output.flush();
                        loginParams.add("username", scanner.next());

                        output.write("\nEnter password: ");
                        output.flush();
                        loginParams.add("password", scanner.next());

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

                        //Connect to the STOMP endpoint
                        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

                        try {
                            //Include the user token and other
                            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
                            webSocketHttpHeaders.add("Authorization", "Bearer " + token);
                            StompHeaders stompHeaders = new StompHeaders();
                            stompHeaders.add("Authorization", "Bearer " + token);
                            stompHeaders.setAck("client-individual");

                            //Subscribe to necessary topics
                            List<String> subs = new ArrayList<>();
                            subs.add("/user/queue/reply");
                            subs.add("/user/queue/errors");
                            subs.add("/topic/broadcast");
                            stompSession = stompClient.connect(STOMP_CONNECT_URL, webSocketHttpHeaders, stompHeaders, new MyStompSessionHandler(subs, queue, output)).get();
                            output.write("\nOpened session with server..\n\n");
                            output.flush();
                        } catch (Exception e) {
                            output.write("\nFailed to connect to UI service.. [" + e.getMessage() + "]");
                            output.flush();
                            break;
                        }
                        break;

                    case 3:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

                        //Arguments
                        output.write("\nQueueing up for a practice play. Enter preferred game type: ");
                        output.flush();
                        GameTypeEnum gameType = GameTypeEnum.valueOf(scanner.next());
                        output.write("\n" + gameType.toString());
                        output.flush();

                        //Queue up
                        HttpEntity<String> practiceResponse;
                        try {
                            practiceResponse = client.practice(PRACTICE_URL, token, gameType, username);
                        } catch (Exception e) {
                            output.write("\n" + e.getMessage());
                            break;
                        }
                        output.write("\nPractice play enqueued status: " + practiceResponse.getBody());
                        output.flush();
                        break;

                    case 4:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

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
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

                        output.write("\nCreating a tournament play. Enter preferred game type,num of participants and tournament ID: ");
                        GameTypeEnum tournamentGameType = GameTypeEnum.valueOf(scanner.next());
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

                    case 6:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }


                        boolean finished = false;
                        String playID = "";

                        while (!finished) {
                            output.write("\nWaiting for server...");
                            output.flush();
                            DefaultSTOMPMessage srvMessage = queue.poll(5000, TimeUnit.MILLISECONDS);

                            if (ctrlC) {
                                ctrlC = false;
                                output.write("\nLeaving game..");
                                output.flush();
                                if (srvMessage != null) {
                                    stompSession.acknowledge(srvMessage.getAck(), false);
                                }
                                break;
                            }

                            if (srvMessage == null) {
                                continue;
                            }

                            //Process incoming messages
                            switch (srvMessage.getMessageType()) {
                                case GAME_START:
                                    playID = srvMessage.getID();
                                    output.write(String.format("\nGame with id=[%s] started against [%s].", playID, srvMessage.getPayload()));
                                    StompHeaders stompHeaders = new StompHeaders();
                                    stompHeaders.add("Authorization", token);
                                    stompHeaders.setDestination("/app/play");
                                    stompSession.send(stompHeaders, playID);
                                    break;
                                case NEED_TO_MOVE:
                                    output.write(String.format("\n%s ", srvMessage.getPayload()));
                                    output.flush();
                                    String newMove = scanner.next();
                                    stompSession.send("/app/move", new DefaultSTOMPMessage("", newMove, STOMPMessageType.NEW_MOVE, null, playID));
                                    break;
                                case GAME_OVER:
                                    output.write("\nGame result: " + srvMessage.getPayload());
                                    finished = true;
                                    break;
                                default:
                                    //Non-interactive messages are handled in the session handler
                            }

                            output.write("\n");
                            output.flush();

                            //Acknowledge the message
                            stompSession.acknowledge(srvMessage.getAck(), true);
                        }

                        //Disconnect and continue
                        stompSession.disconnect();
                        output.write("\nDisconnected from server!");
                        output.flush();
                        break;

                    case 8:
                        //Arguments
                        output.write("\nEnter username to search for.\nAnswer: ");
                        output.flush();
                        String usernameForStats = scanner.next();
                        output.write("\nEnter play type to search for.\nAnswer: ");
                        output.flush();
                        String gameTypeForSearch = PlayTypeEnum.valueOf(scanner.next()).name();

                        HttpEntity<String> scoreSearchResponse;
                        try {
                            scoreSearchResponse = client.searchStats(SCORE_URL, gameTypeForSearch, username, token);
                        } catch (Exception e) {
                            output.write("\n" + e.getMessage());
                            output.flush();
                            return -1;
                        }

                        //Token
                        String selfSearchResult = scoreSearchResponse.getBody();
                        output.write(String.format("\nResult for %s,%s: [%s]", usernameForStats, gameTypeForSearch, selfSearchResult));
                        output.flush();
                        break;

                    case 9:
                        //Arguments
                        output.write("\nSearch username: ");
                        output.flush();
                        String usernameToSearch = scanner.next();
                        output.write("\n" + usernameToSearch);
                        output.flush();

                        HttpEntity<String> searchResponse;
                        try {
                            searchResponse = client.searchUser(SEARCH_URL, usernameToSearch, token);
                        } catch (Exception e) {
                            output.write("\n" + e.getMessage());
                            output.flush();
                            return -1;
                        }

                        //Token
                        String searchResult = searchResponse.getBody();
                        output.write("\nResult: " + searchResult);
                        output.flush();
                        break;

                    //Exit
                    case 10:
                        output.write("\n\nExiting..");
                        output.flush();
                        return 0;

                    //Handle errors
                    default:
                        throw new IllegalStateException("Invalid option");
                }
            } catch (Exception e) {
                output.write("\n\nError: " + e.getMessage());
                output.flush();
            }
        }
    }

    private static CommandLine parseInput(String[] args) {
        Options options = new Options();

        Option output = new Option("f", "fromFile", true, "Get user input from a resource file instead of STDIN.");
        output.setRequired(false);
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