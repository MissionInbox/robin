package mi;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import com.mimecast.robin.main.Factories;
import com.mimecast.robin.smtp.session.Session;
import com.mimecast.robin.util.Sleep;
import org.junit.jupiter.api.Disabled;
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

    @Test
    @Disabled // Manual slow test for OOO responses.
    void inboundBounceReceipt() throws AssertException, IOException {
        // First, send the outbound message we want to have an OOO response for.
        Client client = new Client()
                .send("src/test/resources/cases/config/mi/submission.auth.staging.json5");

        // Create a new session and store the original message ID as a magic variable.
        Session session = Factories.getSession();
        session.putMagic("originalMsgId", client.getSession().getEnvelopes().getLast().getMessageId());

        Sleep.nap(5000); // Wait 5 seconds to ensure the outbound got delivered.

        // Now send the inbound OOO message in reply to the first.
        new Client()
                .send("src/test/resources/cases/config/mi/inbound.bounce.staging.json5");
    }
}
