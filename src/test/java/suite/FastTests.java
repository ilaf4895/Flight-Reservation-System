package suite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import tests.ReservationManagerTest;
import tests.PaymentProcessorTest;

@Suite
@SelectClasses({ReservationManagerTest.class, PaymentProcessorTest.class})
@IncludeTags("fast")
public class FastTests {
    // This will run all tests tagged with "fast"
}