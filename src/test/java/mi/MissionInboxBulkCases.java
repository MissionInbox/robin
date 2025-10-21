package mi;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Mission Inbox bulk test cases.
 */
@Tag("bulk")
@Execution(ExecutionMode.CONCURRENT)
public class MissionInboxBulkCases {

    /**
     * Provides parameter sets for the parameterized test.
     *
     * Parameters per invocation:
     *  - taskId: 0..tasks-1
     *  - logicalThread: taskId modulo threads (for easier grouping/tracking)
     *  - casePath: path to the case configuration file
     *
     * Configuration (system properties; defaults in parentheses):
     *  - bulk.tasks (100)
     *  - bulk.threads (10)
     *  - bulk.casePath (src/test/resources/cases/config/mi/submission.auth.staging.json5)
     */
    static Stream<Arguments> bulkMatrix() {
        final int tasks = getIntProperty("bulk.tasks", 100);
        final int threads = Math.max(1, getIntProperty("bulk.threads", 10));
        final String casePath = System.getProperty(
                "bulk.casePath",
                "src/test/resources/cases/config/mi/submission.auth.staging.json5"
        );

        return IntStream.range(0, Math.max(0, tasks))
                .mapToObj(taskId -> Arguments.of(taskId, taskId % threads, casePath));
    }

    /**
     * Parameterized test for bulk submission with authentication.
     *
     * @param taskId        the unique task identifier
     * @param logicalThread the logical thread number for grouping
     * @param casePath     the path to the case configuration file
     * @throws AssertException if an assertion fails during client operation
     * @throws IOException     if an I/O error occurs during client operation
     */
    @DisplayName("Bulk submission with authentication")
    @ParameterizedTest(name = "[{index}] task={0}, thread={1}")
    @MethodSource("bulkMatrix")
    void bulkSubmissionWithAuthentication(int taskId, int logicalThread, String casePath) throws AssertException, IOException {
        // Helpful trace to correlate per-invocation execution with actual worker thread.
        System.out.printf("Starting task=%d logicalThread=%d on worker=%s using case=%s%n",
                taskId, logicalThread, Thread.currentThread().getName(), casePath);

        new Client().send(casePath);

        // Optional completion trace.
        System.out.printf("Completed task=%d logicalThread=%d on worker=%s%n",
                taskId, logicalThread, Thread.currentThread().getName());
    }

    /**
     * Retrieves an integer system property with a default value.
     *
     * @param key the system property key
     * @param def the default value if the property is not set or invalid
     * @return the integer value of the system property or the default
     */
    private static int getIntProperty(String key, int def) {
        final String raw = System.getProperty(key);
        if (raw == null || raw.isBlank()) return def;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
