package mi;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Tag("bulk")
public class MissionInboxBulkCases {

    /**
     * A simple Runnable task that runs the Client send method.
     */
    static class CaseRunner implements Runnable {

        @Override
        public void run() {
            try {
                new Client()
                        .send("src/test/resources/cases/config/mi/submission.auth.staging.json5");
            } catch (AssertException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void bulkSubmissionWithAuthentication() {
        int threads = 2;
        int tasks = 6;

        // Create a fixed-size thread pool using an ExecutorService.
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // This loop submits and runs N tasks to the thread pool.
        for (int i = 0; i < tasks; i++) {
            executor.execute(new CaseRunner());
        }

        // Initiates an orderly shutdown. No new tasks will be accepted,
        // but previously submitted tasks will be executed.
        executor.shutdown();
        try {
            // The awaitTermination() method blocks the main thread's execution
            // until all tasks have completed after a shutdown request.
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // If the main thread is interrupted, we re-interrupt it to preserve the interrupt status.
            Thread.currentThread().interrupt();
        }
    }
}
