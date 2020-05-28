package main;

import main.GameClient;

import java.io.File;

public class UserActionTask implements Runnable {
    private final File file;
    private boolean silent;

    public UserActionTask(File file, boolean silent) {
        this.file = file;
        this.silent = silent;
    }

    public void run() {
        try {
            GameClient.main(new String[]{
                    "-f",
                    file.getAbsolutePath(),
                    "-s",
                    String.valueOf(silent)
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
