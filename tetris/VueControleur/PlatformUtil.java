
package tetris.VueControleur;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;

/**
 * Provides a few helper methods for the JavaFX Platform
 */
public class PlatformUtil {

    /**
     * Calls Platform.runLater to run the runnable but waits until it has
     * finished execution.
     * Waiting is implemented by means of a CountDownLatch.
     * @param runnable
     */
    public static void platformRunAndWait(final Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException("runnable");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        // create latch
        CountDownLatch latch = new CountDownLatch(1);

        // extend runnable with latch count down
        Runnable r = () -> {
            try {
                runnable.run();
            } finally {
                latch.countDown();
            }
        };

        // run the extended runnable through Platform.runLater()
        Platform.runLater(r);

        // wait for the latch
        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore
        }

    }


    static public void runFutureTask(final Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException("runnable");

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }

        // run the FutureTask with Platform.runLater() and query the
        // result with get() which blocks until the task is done.
        try {
            FutureTask<Void> future = new FutureTask<>(runnable, null);
            Platform.runLater(future);
            future.get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
