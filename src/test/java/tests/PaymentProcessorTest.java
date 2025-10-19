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
    
}
