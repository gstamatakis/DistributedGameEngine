package main;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class GameClientTest {
    private static final Logger logger = LoggerFactory.getLogger(GameClientTest.class);
    private static final String clientActionsPath = "C:\\Users\\gstamatakis\\IdeaProjects\\DistributedGameEngine\\input\\clients";
    private static File[] clientActionFiles;
    private static ExecutorService executorService;

    @BeforeAll
    static void setUp() {
        clientActionFiles = getClientActionFiles(clientActionsPath);
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
    public void concurrent_users_test() {
        //Submit user actions
        List<Future<String>> futures = new ArrayList<>();
        for (File file : clientActionFiles) {
            Future<String> future = executorService.submit(new UserActionTask(file, true));
            futures.add(future);
        }
        logger.info("Invoked all actions.");

        //Wait for futures to complete
        for (Future<String> future : futures) {
            try {
                String res = future.get();
                logger.info(res);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        logger.info("Completed the processing of all Futures.");
    }

    public static class UserActionTask implements Callable<String> {
        private final File file;
        private boolean silent;

        public UserActionTask(File file) {
            this.file = file;
        }

        public UserActionTask(File file, boolean silent) {
            this.file = file;
            this.silent = silent;
        }

        public String call() throws Exception {
            GameClient.main(new String[]{
                    "-f",
                    file.getAbsolutePath(),
                    "-s",
                    String.valueOf(silent)
            });
            return "";
        }
    }

    private static File[] getClientActionFiles(String dirPath) {
        File dir = new File(dirPath);
        return dir.listFiles((dir1, name) -> name.startsWith("client_actions_") && name.endsWith(".txt"));
    }
}