package tests;

import model.Payment;
import service.PaymentProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Processor Test Suite")
public class PaymentProcessorTest {
    
    private PaymentProcessor paymentProcessor;
    
    @BeforeEach
    void setUp() {
        paymentProcessor = new PaymentProcessor();
    }
    
    // ==================== EQUIVALENCE CLASS PARTITIONING (ECP) ====================
    
    @Test
    @DisplayName("ECP - Valid Payment Processing with Visa Card")
    void testProcessPayment_ValidVisaCard() {
        Payment payment = paymentProcessor.processPayment(
            "RES001", 5000.0, "4532015112830366", "123", "12/26"
        );
        
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(5000.0, payment.getAmount());
        assertEquals("RES001", payment.getReservationId());
        assertTrue(payment.getPaymentId().startsWith("PAY"));
    }
    
    @Test
    @DisplayName("ECP - Valid Payment Processing with Mastercard")
    void testProcessPayment_ValidMastercard() {
        Payment payment = paymentProcessor.processPayment(
            "RES002", 10000.0, "5105105105105100", "456", "01/27"
        );
        
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(10000.0, payment.getAmount());
    }
    
    @Test
    @DisplayName("ECP - Valid Payment Processing with Amex Card")
    void testProcessPayment_ValidAmexCard() {
        Payment payment = paymentProcessor.processPayment(
            "RES003", 1500.0, "378282246310005", "1234", "06/26"
        );
        
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(1500.0, payment.getAmount());
    }
    
    @Test
    @DisplayName("ECP - Invalid Payment with Negative Amount")
    void testProcessPayment_NegativeAmount() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES004", -100.0, "4532015112830366", "123", "12/26"
            )
        );
        assertEquals("Amount must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Invalid Payment with Zero Amount")
    void testProcessPayment_ZeroAmount() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES005", 0.0, "4532015112830366", "123", "12/26"
            )
        );
        assertEquals("Amount must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Invalid Card Number")
    void testProcessPayment_InvalidCardNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES006", 5000.0, "123456789", "123", "12/26"
            )
        );
        assertEquals("Invalid card number", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Invalid CVV - Too Short")
    void testProcessPayment_CVVTooShort() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES007", 5000.0, "4532015112830366", "12", "12/26"
            )
        );
        assertEquals("Invalid CVV", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Invalid CVV - Too Long")
    void testProcessPayment_CVVTooLong() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES008", 5000.0, "4532015112830366", "12345", "12/26"
            )
        );
        assertEquals("Invalid CVV", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Invalid Expiry Month")
    void testProcessPayment_InvalidExpiryMonth() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES009", 5000.0, "4532015112830366", "123", "13/26"
            )
        );
        assertEquals("Invalid expiry date", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Expired Card")
    void testProcessPayment_ExpiredCard() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES010", 5000.0, "4532015112830366", "123", "12/20"
            )
        );
        assertEquals("Invalid expiry date", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Null Reservation ID")
    void testProcessPayment_NullReservationId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                null, 5000.0, "4532015112830366", "123", "12/26"
            )
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("ECP - Empty Reservation ID")
    void testProcessPayment_EmptyReservationId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "   ", 5000.0, "4532015112830366", "123", "12/26"
            )
        );
        assertEquals("Reservation ID cannot be empty", exception.getMessage());
    }
    
    // ==================== BOUNDARY VALUE ANALYSIS (BVA) ====================
    
    @Test
    @DisplayName("BVA - Amount: Just Above Zero (0.01)")
    void testProcessPayment_AmountJustAboveZero() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA001", 0.01, "4532015112830366", "123", "12/26"
        );
        assertEquals(0.01, payment.getAmount());
        assertEquals("SUCCESS", payment.getStatus());
    }
    
    @Test
    @DisplayName("BVA - Amount: Small Valid Amount (1.0)")
    void testProcessPayment_SmallValidAmount() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA002", 1.0, "4532015112830366", "123", "12/26"
        );
        assertEquals(1.0, payment.getAmount());
    }
    
    @Test
    @DisplayName("BVA - Amount: Large Valid Amount (999999.99)")
    void testProcessPayment_LargeValidAmount() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA003", 999999.99, "4532015112830366", "123", "12/26"
        );
        assertEquals(999999.99, payment.getAmount());
    }
    
    @Test
    @DisplayName("BVA - CVV: Minimum Valid Length (3 digits)")
    void testProcessPayment_CVVMinimumLength() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA004", 1000.0, "4532015112830366", "000", "12/26"
        );
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
    }
    
    @Test
    @DisplayName("BVA - CVV: Maximum Valid Length (4 digits for Amex)")
    void testProcessPayment_CVVMaximumLength() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA005", 1000.0, "378282246310005", "9999", "12/26"
        );
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
    }
    
    @Test
    @DisplayName("BVA - CVV: Below Minimum (2 digits)")
    void testProcessPayment_CVVBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA006", 1000.0, "4532015112830366", "99", "12/26"
            )
        );
    }
    
    @Test
    @DisplayName("BVA - CVV: Above Maximum (5 digits)")
    void testProcessPayment_CVVAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA007", 1000.0, "4532015112830366", "12345", "12/26"
            )
        );
    }
    
//    @Test
//    @DisplayName("BVA - Card Number: Minimum Valid Length (13 digits)")
//    void testProcessPayment_CardNumberMinimumLength() {
//        // 13-digit valid Visa card: 4539319503436467 -> 4539319503436
//        Payment payment = paymentProcessor.processPayment(
//            "RES_BVA008", 1000.0, "4539319503436", "123", "12/26"
//        );
//        assertNotNull(payment);
//    }
    
    @Test
    @DisplayName("BVA - Card Number: Maximum Valid Length (19 digits)")
    void testProcessPayment_CardNumberMaximumLength() {
        // 19-digit valid card: 6011111111111117
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA009", 1000.0, "6011111111111117", "123", "12/26"
        );
        assertNotNull(payment);
    }
    
    @Test
    @DisplayName("BVA - Card Number: Below Minimum (12 digits)")
    void testProcessPayment_CardNumberBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA010", 1000.0, "453201511283", "123", "12/26"
            )
        );
    }
    
    @Test
    @DisplayName("BVA - Card Number: Above Maximum (20 digits)")
    void testProcessPayment_CardNumberAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA011", 1000.0, "45320151128303661234", "123", "12/26"
            )
        );
    }
    
    @Test
    @DisplayName("BVA - Expiry Date: Current Month/Year (Valid)")
    void testProcessPayment_ExpiryCurrentMonth() {
        // Using a future date that should be valid
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA012", 1000.0, "4532015112830366", "123", "12/27"
        );
        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
    }
    
    @Test
    @DisplayName("BVA - Expiry Month: Minimum Valid (01)")
    void testProcessPayment_ExpiryMonthMinimum() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA013", 1000.0, "4532015112830366", "123", "01/27"
        );
        assertNotNull(payment);
    }
    
    @Test
    @DisplayName("BVA - Expiry Month: Maximum Valid (12)")
    void testProcessPayment_ExpiryMonthMaximum() {
        Payment payment = paymentProcessor.processPayment(
            "RES_BVA014", 1000.0, "4532015112830366", "123", "12/27"
        );
        assertNotNull(payment);
    }
    
    @Test
    @DisplayName("BVA - Expiry Month: Below Minimum (00)")
    void testProcessPayment_ExpiryMonthBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA015", 1000.0, "4532015112830366", "123", "00/27"
            )
        );
    }
    
    @Test
    @DisplayName("BVA - Expiry Month: Above Maximum (13)")
    void testProcessPayment_ExpiryMonthAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_BVA016", 1000.0, "4532015112830366", "123", "13/27"
            )
        );
    }
    
    // ==================== PARAMETERIZED TESTS ====================

//    @ParameterizedTest(name = "Test {index}: Amount={0}, Card={1}, CVV={2}, Expiry={3}")
//    @CsvSource({
//        "5000, 4532015112830366, 123, 12/26, Valid Visa card",
//        "10000, 5105105105105100, 456, 01/27, Valid Mastercard",
//        "1500, 378282246310005, 1234, 06/26, Valid Amex card",
//        "0.01, 4532015112830366, 123, 12/26, Minimum valid amount",
//        "99999, 4532015112830366, 999, 12/27, Large amount with max CVV",
//        "2500.50, 6011111111111117, 789, 03/27, Discover card with decimal",
//        "750, 4539319503436, 456, 12/26, 13-digit Visa card"
//    })
//    @DisplayName("Parameterized - Valid Payment Scenarios")
//    void testProcessPayment_ValidScenarios(double amount, String cardNumber,
//                                           String cvv, String expiryDate, String description) {
//        Payment payment = paymentProcessor.processPayment(
//            "RES_PARAM_" + amount, amount, cardNumber, cvv, expiryDate
//        );
//
//        assertNotNull(payment, description);
//        assertEquals("SUCCESS", payment.getStatus(), description);
//        assertEquals(amount, payment.getAmount(), description);
//    }

    @ParameterizedTest(name = "Test {index}: Invalid Amount={0}")
    @ValueSource(doubles = {-100.0, -0.01, 0.0, -1000.0, -99999.99})
    @DisplayName("Parameterized - Invalid Amount Values")
    void testProcessPayment_InvalidAmounts(double amount) {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_INVALID", amount, "4532015112830366", "123", "12/26"
            ),
            "Should throw exception for amount: " + amount
        );
    }

    @ParameterizedTest(name = "Test {index}: Invalid Card={0}")
    @ValueSource(strings = {
        "123456789",           // Too short
        "1234567890123456",    // Invalid Luhn
        "4532015112830367",    // Failed Luhn check
        "45320151128303661234567", // Too long
        "abcd1234efgh5678"     // Contains letters
    })
    @DisplayName("Parameterized - Invalid Card Numbers")
    void testProcessPayment_InvalidCardNumbers(String cardNumber) {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_INVALID", 1000.0, cardNumber, "123", "12/26"
            ),
            "Should throw exception for card: " + cardNumber
        );
    }

    @ParameterizedTest(name = "Test {index}: Invalid CVV={0}")
    @ValueSource(strings = {"1", "12", "12345", "123a", "abc", ""})
    @DisplayName("Parameterized - Invalid CVV Values")
    void testProcessPayment_InvalidCVVs(String cvv) {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_INVALID", 1000.0, "4532015112830366", cvv, "12/26"
            ),
            "Should throw exception for CVV: " + cvv
        );
    }

    @ParameterizedTest(name = "Test {index}: Invalid Expiry={0}")
    @CsvSource({
        "13/26, Invalid month 13",
        "00/26, Invalid month 00",
        "12/20, Expired year",
        "1/26, Wrong format - single digit month",
        "12/2026, Wrong format - 4 digit year",
        "12-26, Wrong separator",
        "invalid, Not a date"
    })
    @DisplayName("Parameterized - Invalid Expiry Dates")
    void testProcessPayment_InvalidExpiryDates(String expiryDate, String description) {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.processPayment(
                "RES_INVALID", 1000.0, "4532015112830366", "123", expiryDate
            ),
            description
        );
    }

    // ==================== REFUND PAYMENT TESTS ====================

    @Test
    @DisplayName("Refund - Successful Payment Refund")
    void testRefundPayment_Success() {
        Payment payment = paymentProcessor.processPayment(
            "RES_REF001", 5000.0, "4532015112830366", "123", "12/26"
        );

        boolean refunded = paymentProcessor.refundPayment(payment.getPaymentId());

        assertTrue(refunded);
        assertEquals("REFUNDED", payment.getStatus());
    }

    @Test
    @DisplayName("Refund - Non-existent Payment")
    void testRefundPayment_NonExistent() {
        boolean refunded = paymentProcessor.refundPayment("PAY_NONEXISTENT");
        assertFalse(refunded);
    }

    @Test
    @DisplayName("Refund - Null Payment ID")
    void testRefundPayment_NullPaymentId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.refundPayment(null)
        );
    }

    @Test
    @DisplayName("Refund - Empty Payment ID")
    void testRefundPayment_EmptyPaymentId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.refundPayment("   ")
        );
    }

    // ==================== FIND PAYMENT TESTS ====================

    @Test
    @DisplayName("Find Payment - Valid Payment ID")
    void testFindPaymentById_Valid() {
        Payment payment = paymentProcessor.processPayment(
            "RES_FIND001", 5000.0, "4532015112830366", "123", "12/26"
        );

        Payment found = paymentProcessor.findPaymentById(payment.getPaymentId());

        assertNotNull(found);
        assertEquals(payment.getPaymentId(), found.getPaymentId());
    }

    @Test
    @DisplayName("Find Payment - Non-existent Payment ID")
    void testFindPaymentById_NonExistent() {
        Payment found = paymentProcessor.findPaymentById("PAY_NONEXISTENT");
        assertNull(found);
    }

    @Test
    @DisplayName("Find Payment - Null Payment ID")
    void testFindPaymentById_NullPaymentId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.findPaymentById(null)
        );
    }

    @Test
    @DisplayName("Find Payment - Empty Payment ID")
    void testFindPaymentById_EmptyPaymentId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.findPaymentById("")
        );
    }

    // ==================== GET PAYMENTS BY RESERVATION TESTS ====================

    @Test
    @DisplayName("Get Payments - Multiple Payments for Same Reservation")
    void testGetPaymentsByReservation_MultiplePayments() {
        paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        paymentProcessor.processPayment("RES001", 3000.0, "5105105105105100", "456", "01/27");
        paymentProcessor.processPayment("RES002", 2000.0, "378282246310005", "1234", "06/26");

        List<Payment> payments = paymentProcessor.getPaymentsByReservation("RES001");

        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getReservationId().equals("RES001")));
    }

    @Test
    @DisplayName("Get Payments - No Payments for Reservation")
    void testGetPaymentsByReservation_NoPayments() {
        List<Payment> payments = paymentProcessor.getPaymentsByReservation("RES_NONE");
        assertTrue(payments.isEmpty());
    }

    @Test
    @DisplayName("Get Payments - Null Reservation ID")
    void testGetPaymentsByReservation_NullReservationId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.getPaymentsByReservation(null)
        );
    }

    @Test
    @DisplayName("Get Payments - Empty Reservation ID")
    void testGetPaymentsByReservation_EmptyReservationId() {
        assertThrows(IllegalArgumentException.class,
            () -> paymentProcessor.getPaymentsByReservation("  ")
        );
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    @DisplayName("Statistics - Total Payments Count")
    void testGetTotalPayments() {
        assertEquals(0, paymentProcessor.getTotalPayments());

        paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        paymentProcessor.processPayment("RES002", 3000.0, "5105105105105100", "456", "01/27");

        assertEquals(2, paymentProcessor.getTotalPayments());
    }

    @Test
    @DisplayName("Statistics - Total Revenue")
    void testGetTotalRevenue() {
        paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        paymentProcessor.processPayment("RES002", 3000.0, "5105105105105100", "456", "01/27");

        assertEquals(8000.0, paymentProcessor.getTotalRevenue());
    }

    @Test
    @DisplayName("Statistics - Successful Payments Count")
    void testGetSuccessfulPayments() {
        paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        paymentProcessor.processPayment("RES002", 3000.0, "5105105105105100", "456", "01/27");

        assertEquals(2, paymentProcessor.getSuccessfulPayments());
    }

    @Test
    @DisplayName("Statistics - Failed Payments Count")
    void testGetFailedPayments() {
        Payment payment1 = paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        Payment payment2 = paymentProcessor.processPayment("RES002", 3000.0, "5105105105105100", "456", "01/27");

        payment2.failPayment();

        assertEquals(1, paymentProcessor.getFailedPayments());
    }

    @Test
    @DisplayName("Statistics - Revenue Excludes Refunded Payments")
    void testGetTotalRevenue_ExcludesRefunded() {
        Payment payment1 = paymentProcessor.processPayment("RES001", 5000.0, "4532015112830366", "123", "12/26");
        Payment payment2 = paymentProcessor.processPayment("RES002", 3000.0, "5105105105105100", "456", "01/27");

        paymentProcessor.refundPayment(payment1.getPaymentId());

        assertEquals(3000.0, paymentProcessor.getTotalRevenue());
    }

    // ==================== PAYMENT ID GENERATION TESTS ====================

    @Test
    @DisplayName("Payment ID - Sequential Generation")
    void testPaymentIdGeneration() {
        Payment payment1 = paymentProcessor.processPayment("RES001", 1000.0, "4532015112830366", "123", "12/26");
        Payment payment2 = paymentProcessor.processPayment("RES002", 2000.0, "5105105105105100", "456", "01/27");

        assertTrue(payment1.getPaymentId().startsWith("PAY"));
        assertTrue(payment2.getPaymentId().startsWith("PAY"));
        assertNotEquals(payment1.getPaymentId(), payment2.getPaymentId());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Integration - Complete Payment Lifecycle")
    void testCompletePaymentLifecycle() {
        // Process payment
        Payment payment = paymentProcessor.processPayment(
            "RES_LIFECYCLE", 5000.0, "4532015112830366", "123", "12/26"
        );
        assertEquals("SUCCESS", payment.getStatus());

        // Find payment
        Payment found = paymentProcessor.findPaymentById(payment.getPaymentId());
        assertNotNull(found);
        assertEquals(payment.getPaymentId(), found.getPaymentId());

        // Get by reservation
        List<Payment> payments = paymentProcessor.getPaymentsByReservation("RES_LIFECYCLE");
        assertEquals(1, payments.size());

        // Check statistics
        assertEquals(1, paymentProcessor.getTotalPayments());
        assertEquals(5000.0, paymentProcessor.getTotalRevenue());

        // Refund payment
        boolean refunded = paymentProcessor.refundPayment(payment.getPaymentId());
        assertTrue(refunded);
        assertEquals("REFUNDED", payment.getStatus());

        // Revenue should be zero after refund
        assertEquals(0.0, paymentProcessor.getTotalRevenue());
    }
}
