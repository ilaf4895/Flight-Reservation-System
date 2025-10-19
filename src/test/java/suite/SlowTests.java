package suite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import tests.PaymentProcessorTest;
import tests.ReservationManagerTest;

@Suite
@SelectClasses({ReservationManagerTest.class, PaymentProcessorTest.class})
@IncludeTags("slow")
public class SlowTests {
    // This will run all tests tagged with "slow"
}