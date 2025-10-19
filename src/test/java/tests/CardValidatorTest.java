package tests;


import model.Payment;

/**
 * Quick standalone test to verify card numbers work
 * Run this to check if Luhn algorithm is working correctly
 */
public class CardValidatorTest {
    
    public static void main(String[] args) {
        System.out.println("=== CARD VALIDATION TEST ===\n");
        
        // Test valid cards
        System.out.println("VALID CARDS (should all be true):");
        testCard("4532015112830366", "Visa");
        testCard("5425233430103487", "Mastercard");
        testCard("378282246310005", "American Express");
        testCard("6011111111111117", "Discover");
        testCard("3530111333300000", "JCB");
        
        System.out.println("\nINVALID CARDS (should all be false):");
        testCard("1234567890123456", "Random numbers");
        testCard("4532015112830367", "Visa (wrong check digit)");
        testCard("1234567890", "Too short");
        testCard("", "Empty string");
        testCard(null, "Null");
        
        System.out.println("\n=== CVV VALIDATION TEST ===\n");
        testCVV("123", true);
        testCVV("1234", true);
        testCVV("12", false);
        testCVV("12345", false);
        testCVV("abc", false);
        testCVV(null, false);
        
        System.out.println("\n=== EXPIRY DATE VALIDATION TEST ===\n");
        testExpiry("12/25", true);
        testExpiry("01/26", true);
        testExpiry("12/20", false);
        testExpiry("13/25", false);
        testExpiry("00/25", false);
        testExpiry("invalid", false);
        testExpiry(null, false);
        
        System.out.println("\n=== ALL TESTS COMPLETE ===");
    }
    
    private static void testCard(String cardNumber, String type) {
        boolean result = Payment.isValidCardNumber(cardNumber);
        String status = result ? "✓ PASS" : "✗ FAIL";
        System.out.printf("%s - %s: %s%n", status, type, 
                         cardNumber != null ? cardNumber : "null");
    }
    
    private static void testCVV(String cvv, boolean expected) {
        boolean result = Payment.isValidCVV(cvv);
        String status = (result == expected) ? "✓ PASS" : "✗ FAIL";
        System.out.printf("%s - CVV '%s': %s (expected %s)%n", 
                         status, cvv != null ? cvv : "null", result, expected);
    }
    
    private static void testExpiry(String expiry, boolean expected) {
        boolean result = Payment.isValidExpiryDate(expiry);
        String status = (result == expected) ? "✓ PASS" : "✗ FAIL";
        System.out.printf("%s - Expiry '%s': %s (expected %s)%n", 
                         status, expiry != null ? expiry : "null", result, expected);
    }
}