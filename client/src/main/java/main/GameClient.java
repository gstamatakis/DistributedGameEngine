package main;

import com.google.gson.Gson;
import message.completed.CompletedMoveMessage;
import message.created.PlayMessage;
import message.requests.RequestCreateTournamentMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GameClient {
    private static final Logger logger = LoggerFactory.getLogger(GameClient.class);

    private static final String SIGN_IN_URL = "http://localhost:8080/users/signin";
    private static final String SIGNUP_URL = "http://localhost:8080/users/signup";
    private static final String PRACTICE_URL = "http://localhost:8080/queue/practice";
    private static final String SCORE_URL = "http://localhost:8080/users/score";
    private static final String TOURNAMENT_CREATE_URL = "http://localhost:8080/queue/tournament/create";
    private static final String TOURNAMENT_JOIN_URL = "http://localhost:8080/queue/tournament/join";
    private static final String SEARCH_URL = "http://localhost:8080/users/";
    private static final String STOMP_CONNECT_URL = "ws://localhost:8080/play";

    private static boolean ctrlC = false;
    private static final Gson gson = new Gson();

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
        GameTypeEnum selectedGT = null;
        ArrayBlockingQueue<DefaultSTOMPMessage> queue = new ArrayBlockingQueue<>(10);

        //Timer for periodic updates
        Timer timer = new Timer("Timer");

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
                        String temp_username = scanner.next();
                        loginParams.add("username", temp_username);

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
                        username = temp_username;

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
                        output.write("\nQueueing up for a practice play. Enter preferred game type");
                        output.write("\n1.TicTacToe \n2.Chess \nPress another key to return to menu\nAnswer: ");
                        output.flush();
                        String response = scanner.next();
                        output.write("\n");
                        GameTypeEnum gameType;
                        switch (response) {
                            case "1":
                                gameType = GameTypeEnum.TIC_TAC_TOE;
                                break;
                            case "2":
                                gameType = GameTypeEnum.CHESS;
                                break;
                            default:
                                gameType = null;
                        }
                        if (gameType == null) {
                            break;
                        }
                        selectedGT = gameType;
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

                        ConcurrentHashMap<String, String> board = new ConcurrentHashMap<>();
                        switch (selectedGT) {
                            case TIC_TAC_TOE:
                                for (int i = 1; i <= 9; i++) {
                                    board.put(String.valueOf(i), "_");
                                }
                                break;
                            case CHESS:
                                break;
                            default:
                                throw new IllegalStateException("Client does not support this game type.");
                        }

                        boolean finished = false;
                        timer.schedule(new MyTimerTask(stompSession, queue, token, username, output, board), 0, 100L);

                        while (!finished) {
                            long timeoutMS = 300 * 1000;
                            DefaultSTOMPMessage srvMessage = queue.poll(timeoutMS, TimeUnit.MILLISECONDS);

                            if (srvMessage == null) {
                                output.write("\nWaited for " + timeoutMS + " ms. Giving up...");
                                output.flush();
                                break;
                            }

                            //Process incoming messages
                            switch (srvMessage.getMessageType()) {
                                case NEED_TO_MOVE:
                                    //Ask to make a move only if there are no incoming notifications
                                    if (queue.size() != 0) {
                                        queue.add(srvMessage);
                                        continue;
                                    }
                                    String newMovePlayID = srvMessage.getID();
                                    output.write(String.format("\n%s \nAnswer: ", srvMessage.getPayload()));
                                    output.flush();
                                    if (scanner.hasNext()) {
                                        String newMove = scanner.next();
                                        synchronized (stompSession) {
                                            stompSession.send("/app/move", new DefaultSTOMPMessage("", newMove, STOMPMessageType.NEW_MOVE, null, newMovePlayID));
                                        }
                                    }
                                    output.write("\n");
                                    output.flush();
                                    break;
                                case GAME_OVER:
                                    output.write("\nGame result: " + srvMessage.getPayload());
                                    output.write("\n");
                                    output.flush();
                                    finished = true;
                                    break;
                                default:
                                    queue.add(srvMessage);
                            }

                            //Acknowledge the message
                            synchronized (stompSession) {
                                stompSession.acknowledge(srvMessage.getAck(), true);
                            }
                        }

                        //Disconnect and continue
                        timer.cancel(); //Finish this run and stop scheduling this task
                        output.write("\nDisconnected from server!");
                        output.flush();
                        break;

                    case 8:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

                        //Arguments
                        output.write("\nEnter username to search for.\nAnswer: ");
                        output.flush();
                        String usernameForStats = scanner.next();
                        output.write("\nEnter play type to search for.\n1.Practice\n2.Tournament\n3.Press any other key to return to menu. \nAnswer: ");
                        output.flush();
                        String answ8 = scanner.next();
                        String gameTypeForSearch;
                        switch (answ8) {
                            case "1":
                                gameTypeForSearch = PlayTypeEnum.PRACTICE.name();
                                break;
                            case "2":
                                gameTypeForSearch = PlayTypeEnum.TOURNAMENT.name();
                                break;
                            default:
                                gameTypeForSearch = null;
                        }
                        if (gameTypeForSearch == null) {
                            break;
                        }

                        HttpEntity<String> scoreSearchResponse = client.searchStats(SCORE_URL, gameTypeForSearch, usernameForStats, token);

                        //Token
                        String selfSearchResult = scoreSearchResponse.getBody();
                        output.write(String.format("\nResult for %s,%s: [%s]", usernameForStats, gameTypeForSearch, selfSearchResult));
                        output.flush();
                        break;

                    case 9:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

                        //Arguments
                        output.write("\nSearch username: ");
                        output.flush();
                        String usernameToSearch = scanner.next();
                        output.write("\n" + usernameToSearch);
                        output.flush();

                        //Perform the search request
                        HttpEntity<String> searchResponse = client.searchUser(SEARCH_URL, usernameToSearch, token);

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

    static class MyTimerTask extends TimerTask {
        private final StompSession stompSession;
        private final ArrayBlockingQueue<DefaultSTOMPMessage> queue;
        private final String token;
        private final String username;
        private BufferedWriter output;
        private ConcurrentHashMap<String, String> board;

        public MyTimerTask(StompSession stompSession, ArrayBlockingQueue<DefaultSTOMPMessage> queue, String token,
                           String username, BufferedWriter output, ConcurrentHashMap<String, String> board) {
            this.stompSession = stompSession;
            this.queue = queue;
            this.token = token;
            this.username = username;
            this.output = output;
            this.board = board;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DefaultSTOMPMessage srvMessage = queue.poll(0, TimeUnit.MILLISECONDS);

                    if (srvMessage == null) {
                        return;
                    }

                    //Process incoming messages
                    switch (srvMessage.getMessageType()) {
                        case NOTIFICATION:
                            output.write("\nNOTIFICATION: " + srvMessage.getPayload());
                            output.write("\n");
                            output.flush();
                            break;
                        case FETCH_PLAY:
                            PlayMessage playMessage = gson.fromJson(srvMessage.getPayload(), PlayMessage.class);
                            if (playMessage == null) {
                                output.write("\nRetrieved null play..");
                            } else {
                                output.write("\nRetrieved play: " + playMessage.getGameState().getPrintableBoard());
                            }
                            output.write("\n");
                            output.flush();
                            break;
                        case MOVE_ACCEPTED:
                            CompletedMoveMessage successfulMoveMessage2 = gson.fromJson(srvMessage.getPayload(), CompletedMoveMessage.class);
                            output.write("\nNew valid move: " + successfulMoveMessage2.getMoveMessage());
                            board.put(successfulMoveMessage2.getMoveMessage().getMove(), successfulMoveMessage2.getPlayedByUsername());
                            output.write("\n\nBoard\n" + board.toString());
                            output.write("\n");
                            output.flush();
                            break;
                        case MOVE_DENIED:
                            CompletedMoveMessage deniedMoveMessage = gson.fromJson(srvMessage.getPayload(), CompletedMoveMessage.class);
                            output.write("\nMove denied: " + deniedMoveMessage.getMoveMessage());
                            output.write("\nCurrent Board: " + board.toString());
                            output.write("\n");
                            output.flush();
                            break;
                        case ERROR:
                            output.write("\nERROR: " + srvMessage.getPayload());
                            output.write("\n");
                            output.flush();
                            break;
                        case KEEP_ALIVE:
                            break;
                        case GAME_START:
                            String gameStartPlayID = srvMessage.getID();
                            output.write(String.format("\nGame with id=[%s] started against [%s].", gameStartPlayID, srvMessage.getPayload()));
                            StompHeaders stompHeaders = new StompHeaders();
                            stompHeaders.add("Authorization", token);
                            stompHeaders.setDestination("/app/play");
                            synchronized (stompSession) {
                                stompSession.send(stompHeaders, gameStartPlayID);
                            }
                            output.write("\n");
                            output.flush();
                            break;
                        default:
                            queue.add(srvMessage);
                            break;
                    }
                    //Acknowledge the message
                    synchronized (stompSession) {
                        stompSession.acknowledge(srvMessage.getAck(), true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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