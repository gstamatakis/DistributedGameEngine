package main;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class GameClientTest {
    private static final Logger logger = LoggerFactory.getLogger(GameClientTest.class);
    private static File[] clientActionFiles;
    private static ExecutorService executorService;

    @BeforeAll
    static void setUp() {
        clientActionFiles = getClientActionFiles("input/practice");
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
            logger.info(res);
        }
        logger.info("Completed the processing of all Futures.");
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

    private static File[] getClientActionFiles(String resource) {
        String dirName = Thread.currentThread().getContextClassLoader().getResource(resource).getFile();
        File dir = new File(dirName);
        return dir.listFiles((dir1, name) -> name.startsWith("client_actions_") && name.endsWith(".txt"));
    }
}