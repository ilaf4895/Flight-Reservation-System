package suite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import tests.ReservationManagerTest;
import tests.PaymentProcessorTest;

@Suite
@SelectClasses({ReservationManagerTest.class, PaymentProcessorTest.class})
@IncludeTags("integration")
public class IntegrationTests {
    // This will run all tests tagged with "integration"
}