package main;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameClientTests {
    private static final Logger logger = LoggerFactory.getLogger(GameClientTests.class);
    private static final String HOSTNAME = "192.168.1.100";
    private static File[] clientActionFiles;
    private static File[] specialActionFiles4;
    private static File[] specialActionFiles8;
    private static File[] tournamentActionFiles4;
    private static File[] tournamentActionFiles8;
    private static ExecutorService executorService;

    @BeforeAll
    static void setUp() {
        clientActionFiles = getClientActionFiles("input/practice", "client_actions_");
        specialActionFiles4 = getClientActionFiles("input/tournament4", "official_actions_");
        tournamentActionFiles4 = getClientActionFiles("input/tournament4", "tournament_player_actions_");
        specialActionFiles8 = getClientActionFiles("input/tournament8", "official_actions_");
        tournamentActionFiles8 = getClientActionFiles("input/tournament8", "tournament_player_actions_");
        executorService = Executors.newFixedThreadPool(8);
    }

    @AfterAll
    static void tearDownAll() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(0)
    public void concurrent_2_practice_players_test() {
        try {
            //Submit user actions
            List<Future<?>> futures = new ArrayList<>();
            for (File file : Arrays.asList(clientActionFiles).subList(0, 2)) {
                UserActionTask callable = new UserActionTask(file, false, HOSTNAME);
                Future<?> future = executorService.submit(callable);
                futures.add(future);
            }
            logger.info("Invoked all actions.");

            //Wait for futures to complete
            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of all Futures.");
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(1)
    public void concurrent_practice_players_test() {
        try {
            //Submit user actions
            List<Future<?>> futures = new ArrayList<>();
            for (File file : clientActionFiles) {
                UserActionTask callable = new UserActionTask(file, false, HOSTNAME);
                Future<?> future = executorService.submit(callable);
                futures.add(future);
            }
            logger.info("Invoked all actions.");

            //Wait for futures to complete
            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of all Futures.");
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(2)
    public void concurrent_tournament_players_instant_win_test() {
        try {
            //Submit the actions of the Official(s)
            List<Future<?>> officialsActions = new ArrayList<>();
            for (File file : specialActionFiles4) {
                UserActionTask callable = new UserActionTask(file, false, HOSTNAME);
                Future<?> future = executorService.submit(callable);
                officialsActions.add(future);
            }
            for (Future<?> future : officialsActions) {
                future.get(10, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of Official(s).");

            //Submit the tournament players
            List<Future<?>> tournamentPlayerActions = new ArrayList<>();
            for (File file : tournamentActionFiles4) {
                UserActionTask callable = new UserActionTask(file, false);
                Future<?> future = executorService.submit(callable);
                tournamentPlayerActions.add(future);
            }
            for (Future<?> future : tournamentPlayerActions) {
                future.get(60, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of Tournament Players.");
        } catch (TimeoutException ignored) {
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    @Order(3)
    @Disabled
    public void concurrent_tournament_players_2_rounds_test() {
        try {
            //Submit the actions of the Official(s)
            List<Future<?>> officialsActions = new ArrayList<>();
            for (File file : specialActionFiles8) {
                UserActionTask callable = new UserActionTask(file, false, HOSTNAME);
                Future<?> future = executorService.submit(callable);
                officialsActions.add(future);
            }
            for (Future<?> future : officialsActions) {
                future.get(10, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of Official(s).");

            //Submit the tournament players
            List<Future<?>> tournamentPlayerActions = new ArrayList<>();
            for (File file : Arrays.asList(tournamentActionFiles8).subList(0, 8)) {  //All
                UserActionTask callable = new UserActionTask(file, false, HOSTNAME);
                Future<?> future = executorService.submit(callable);
                tournamentPlayerActions.add(future);
            }
            for (Future<?> future : tournamentPlayerActions) {
                future.get(15, TimeUnit.SECONDS);
            }
            logger.info("Completed the processing of Tournament Players.");
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    private static File[] getClientActionFiles(String resource, String prefix) {
        String dirName = Thread.currentThread().getContextClassLoader().getResource(resource).getFile();
        File dir = new File(dirName);
        return dir.listFiles((dir1, name) -> name.startsWith(prefix) && name.endsWith(".txt"));
    }
}