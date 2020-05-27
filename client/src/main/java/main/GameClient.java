package main;

import com.google.gson.Gson;
import game.AbstractGameState;
import game.ChessGameState;
import game.TicTacToeGameState;
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
    private static final Gson gson = new Gson();
    private static String SIGN_IN_URL = "http://localhost:8080/users/signin";
    private static String SIGNUP_URL = "http://localhost:8080/users/signup";
    private static String PRACTICE_URL = "http://localhost:8080/queue/practice";
    private static String SCORE_URL = "http://localhost:8080/users/score";
    private static String PLAY_URL = "http://localhost:8080/users/play";
    private static String SPECTATE_URL = "http://localhost:8080/users/spectate";
    private static String TOURNAMENT_CREATE_URL = "http://localhost:8080/queue/tournament/create";
    private static String TOURNAMENT_JOIN_URL = "http://localhost:8080/queue/tournament/join";
    private static String SIGNUP_OFFICIAL_URL = "http://localhost:8080/users/createofficial";
    private static String STOMP_CONNECT_URL = "ws://localhost:8080/play";
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

        //If the flag host is provided, replace the 'localhost' part of each URL with the host
        if (cmd.hasOption("host")) {
            SIGN_IN_URL = SIGN_IN_URL.replace("localhost", cmd.getOptionValue("host"));
            SIGNUP_URL = SIGNUP_URL.replace("localhost", cmd.getOptionValue("host"));
            PRACTICE_URL = PRACTICE_URL.replace("localhost", cmd.getOptionValue("host"));
            SCORE_URL = SCORE_URL.replace("localhost", cmd.getOptionValue("host"));
            SPECTATE_URL = SPECTATE_URL.replace("localhost", cmd.getOptionValue("host"));
            TOURNAMENT_CREATE_URL = TOURNAMENT_CREATE_URL.replace("localhost", cmd.getOptionValue("host"));
            TOURNAMENT_JOIN_URL = TOURNAMENT_JOIN_URL.replace("localhost", cmd.getOptionValue("host"));
            SIGNUP_OFFICIAL_URL = SIGNUP_OFFICIAL_URL.replace("localhost", cmd.getOptionValue("host"));
            PLAY_URL = PLAY_URL.replace("localhost", cmd.getOptionValue("host"));
            STOMP_CONNECT_URL = STOMP_CONNECT_URL.replace("localhost", cmd.getOptionValue("host"));
        }

        //Handle Ctrl+C
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> ctrlC = true));

        //Execute the main loop
        int result = handleInputs(scanner, output);
        output.write("\nExit code: " + result + "\n");
        output.close();
        System.exit(0);
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
        options.put(9, "Create official (ADMIN ONLY)");
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

                        output.write("\n" + signupParams.toString());
                        output.flush();

                        //Response = executed entity
                        HttpEntity<String> signUpResponse;
                        try {
                            signUpResponse = client.signup(SIGNUP_URL, signupParams, null);
                        } catch (HttpClientErrorException.UnprocessableEntity e1) {
                            output.write("\n" + e1.getMessage());
                            output.flush();
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
                        output.flush();
                        String tournamentID = scanner.next();

                        //Queue up
                        HttpEntity<String> tournamentJoinResponse;
                        try {
                            tournamentJoinResponse = client.joinTournament(TOURNAMENT_JOIN_URL, token, tournamentID);
                            selectedGT = GameTypeEnum.valueOf(tournamentJoinResponse.getBody());
                        } catch (Exception e) {
                            output.write("\n" + e.getMessage());
                            break;
                        }
                        output.write(String.format("\nTournament play of type [%s] enqueued.", tournamentJoinResponse.getBody()));
                        output.flush();
                        break;

                    case 5:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }

                        output.write("\nCreating a tournament play. Enter preferred game type.\n1.TicTacToe\n2.Chess\nOther. Return to menu.\nAnswer: ");
                        output.flush();
                        String choiceGT = scanner.next();
                        GameTypeEnum tournamentGameType;
                        switch (choiceGT) {
                            case "1":
                                tournamentGameType = GameTypeEnum.TIC_TAC_TOE;
                                break;
                            case "2":
                                tournamentGameType = GameTypeEnum.CHESS;
                                break;
                            default:
                                tournamentGameType = null;
                        }
                        if (tournamentGameType == null) {
                            break;
                        }
                        output.write("\nEnter the number of participants (must be greater than 4).\nAnswer: ");
                        output.flush();
                        int numOfParticipants = scanner.nextInt();

                        output.write("\nEnter a Tournament ID that will be used by other players to join.\nAnswer: ");
                        output.flush();
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

                        if (selectedGT == null) {
                            output.write("\nAttempting to retrieve an on-going play");
                            output.flush();
                            try {
                                PlayMessage currentPlay = client.retrievePlay(PLAY_URL, token);
                                if (currentPlay == null) {
                                    output.write(String.format("\nDid not found any plays for user %s.\nConsider queueing up.", username));
                                    output.flush();
                                    break;
                                }
                                selectedGT = currentPlay.getGameTypeEnum();
                                StompHeaders stompHeaders = new StompHeaders();
                                stompHeaders.add("Authorization", token);
                                stompHeaders.setDestination("/app/play");
                                synchronized (stompSession) {
                                    stompSession.send(stompHeaders, currentPlay.getID());
                                }
                                output.write(String.format("\nRetrieved ongoing play [%s] for [%s]", currentPlay.toString(), username));
                                output.flush();
                            } catch (Exception e) {
                                output.write(e.getMessage());
                                output.flush();
                                break;
                            }
                        }

                        ConcurrentHashMap<String, String> board;
                        switch (selectedGT) {
                            case TIC_TAC_TOE:
                                AbstractGameState tempGS1 = new TicTacToeGameState();
                                board = new ConcurrentHashMap<>(tempGS1.initialBoard());
                                break;
                            case CHESS:
                                AbstractGameState tempGS2 = new ChessGameState();
                                board = new ConcurrentHashMap<>(tempGS2.initialBoard());
                                break;
                            default:
                                throw new IllegalStateException("Client does not support this game type.");
                        }

                        boolean finished = false;
                        timer.scheduleAtFixedRate(new MyTimerTask(stompSession, queue, token, username, output, board), 0, 100L);

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

                        //Run once more and print out the last messages before finishing with this play
                        output.flush();
                        break;

                    case 7:
                        if (stompSession == null) {
                            output.write("\nNeed to sign-in first.");
                            output.flush();
                            break;
                        }
                        output.write("\nEnter the play ID of the game.\nAnswer: ");
                        output.flush();
                        String playIDForSpec = scanner.next();
                        boolean specResult = client.spectate(SPECTATE_URL, playIDForSpec, token).getBody();
                        if (!specResult) {
                            output.write("\nAn error occurred and couldn't join the game (invalid ID most likely).");
                            output.flush();
                            break;
                        }
                        output.write("\nDone, you have now joined this game as a spectator\n");
                        output.flush();

                        boolean specFinished = false;
                        Map<String, String> specBoard;
                        switch (selectedGT) {
                            case TIC_TAC_TOE:
                                AbstractGameState tempGS1 = new TicTacToeGameState();
                                specBoard = new ConcurrentHashMap<>(tempGS1.initialBoard());
                                break;
                            case CHESS:
                                AbstractGameState tempGS2 = new ChessGameState();
                                specBoard = new ConcurrentHashMap<>(tempGS2.initialBoard());
                                break;
                            default:
                                throw new IllegalStateException("Client does not support this game type.");
                        }

                        while (!specFinished) {
                            DefaultSTOMPMessage srvMessage = queue.poll(1000, TimeUnit.MILLISECONDS);

                            if (srvMessage == null) {
                                continue;
                            }

                            //Process incoming messages
                            switch (srvMessage.getMessageType()) {
                                case GAME_START:
                                    String gameStartPlayID = srvMessage.getID();
                                    output.write(String.format("\nGame with id=[%s] started between [%s] and [%s].",
                                            gameStartPlayID, srvMessage.getPrincipal(), srvMessage.getPayload()));
                                    StompHeaders stompHeaders = new StompHeaders();
                                    stompHeaders.add("Authorization", token);
                                    stompHeaders.setDestination("/app/play");
                                    synchronized (stompSession) {
                                        stompSession.send(stompHeaders, gameStartPlayID);
                                    }
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
                                    specBoard.put(successfulMoveMessage2.getMoveMessage().getMove(), successfulMoveMessage2.getPlayedByUsername());
                                    output.write("\n\nBoard\n" + specBoard.toString());
                                    output.write("\n");
                                    output.flush();
                                    break;
                                case GAME_OVER:
                                    output.write("\nWINNER: " + srvMessage.getPayload());
                                    output.write("\n");
                                    output.flush();
                                    specFinished = true;
                                    break;
                                default:
                                    //
                            }

                            //Ack messages
                            stompSession.acknowledge(srvMessage.getAck(), true);
                        }
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
                        Map<String, String> signupParams9 = new LinkedHashMap<>();
                        output.write("\nEnter official's username: ");
                        output.flush();
                        signupParams9.put("username", scanner.next());
                        username = signupParams9.get("username");

                        output.write("\nEnter password: ");
                        output.flush();
                        signupParams9.put("password", scanner.next());

                        output.write("\nEnter email: ");
                        output.flush();
                        signupParams9.put("email", scanner.next());

                        output.write("\n" + signupParams9.toString());
                        output.flush();

                        //Response = executed entity
                        try {
                            HttpEntity<String> signUpResponse9 = client.signup(SIGNUP_OFFICIAL_URL, signupParams9, token);
                            output.write("\n" + signUpResponse9.getBody());
                        } catch (HttpClientErrorException.UnprocessableEntity e1) {
                            output.write("\n" + e1.getMessage());
                        }
                        output.flush();
                        break;

                    //Exit
                    case 10:
                        output.write("\n\nExiting..");
                        output.flush();
                        return 0;

                    //Handle errors
                    default:
                        output.write("\nInvalid option\n");
                        output.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();    //TODO remove
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