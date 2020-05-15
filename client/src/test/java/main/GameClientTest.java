package main;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameClientTest {
    private static final Logger logger = LoggerFactory.getLogger(GameClientTest.class);
    private static File[] clientActionFiles;
    private static File[] specialActionFiles;
    private static File[] tournamentActionFiles;
    private static ExecutorService executorService;

    @BeforeAll
    static void setUp() {
        clientActionFiles = getClientActionFiles("input/practice", "client_actions_");
        specialActionFiles = getClientActionFiles("input/special", "official_actions_");
        tournamentActionFiles = getClientActionFiles("input/tournament", "tournament_player_actions_");
        executorService = new ForkJoinPool(4);
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
    @Order(1)
    @Disabled
    public void concurrent_practice_players_test() throws ExecutionException, InterruptedException {
        //Submit user actions
        List<Future<String>> futures = new ArrayList<>();
        for (File file : clientActionFiles) {
            UserActionTask callable = new UserActionTask(file, false);
            Future<String> future = executorService.submit(callable);
            futures.add(future);
        }
        logger.info("Invoked all actions.");

        //Wait for futures to complete
        for (Future<String> future : futures) {
            String res = future.get();
            Assertions.assertEquals("CALLABLE_OK", res);
        }
        logger.info("Completed the processing of all Futures.");
    }

    @Test
    @Order(1)
    public void concurrent_tournament_players_test() throws ExecutionException, InterruptedException {
        //Submit the actions of the Official(s)
        List<Future<String>> officialsActions = new ArrayList<>();
        for (File file : specialActionFiles) {
            UserActionTask callable = new UserActionTask(file, false);
            Future<String> future = executorService.submit(callable);
            officialsActions.add(future);
        }
        for (Future<String> future : officialsActions) {
            String res = future.get();
            Assertions.assertEquals("CALLABLE_OK", res);
        }
        logger.info("Completed the processing of Official(s).");

        //Submit the tournament players
        List<Future<String>> tournamentPlayerActions = new ArrayList<>();
        for (File file : tournamentActionFiles) {
            UserActionTask callable = new UserActionTask(file, false);
            Future<String> future = executorService.submit(callable);
            tournamentPlayerActions.add(future);
        }
        for (Future<String> future : tournamentPlayerActions) {
            String res = future.get();
            Assertions.assertEquals("CALLABLE_OK", res);
        }
        logger.info("Completed the processing of Tournament Players.");
    }

    private static class UserActionTask implements Callable<String> {
        private final File file;
        private boolean silent;

        public UserActionTask(File file, boolean silent) {
            this.file = file;
            this.silent = silent;
        }

        public String call() {
            try {
                GameClient.main(new String[]{
                        "-f",
                        file.getAbsolutePath(),
                        "-s",
                        String.valueOf(silent)
                });
                return "CALLABLE_OK";
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    }

    private static File[] getClientActionFiles(String resource, String prefix) {
        String dirName = Thread.currentThread().getContextClassLoader().getResource(resource).getFile();
        File dir = new File(dirName);
        return dir.listFiles((dir1, name) -> name.startsWith(prefix) && name.endsWith(".txt"));
    }
}