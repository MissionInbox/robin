package cases;

import com.mimecast.robin.assertion.AssertException;
import com.mimecast.robin.main.Client;
import com.mimecast.robin.main.Foundation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.IOException;

public class MissionInboxStaging {

    @BeforeAll
    static void before() throws ConfigurationException {
        Foundation.init("cfg/");
    }

    @Test
    void submissionWithAuthentication() throws AssertException, IOException {
        new Client()
                .send("src/test/resources/cases/config/mi/submission.auth.staging.json5");
    }
}
