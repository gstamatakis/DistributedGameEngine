package main;

import java.io.File;

public class UserActionTask implements Runnable {
    private final File file;
    private boolean silent;
    private String host;

    public UserActionTask(File file, boolean silent, String host) {
        this.file = file;
        this.silent = silent;
        this.host = host;
    }

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
                    String.valueOf(silent),
                    "-h",
                    host
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
