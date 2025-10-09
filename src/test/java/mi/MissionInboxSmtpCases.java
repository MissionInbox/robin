package mi;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Tag("smtp")
public class MissionInboxSmtpCases {

    @Test
    void submissionWithAuthentication() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/mi/submission.auth.staging.json5");
    }
}
