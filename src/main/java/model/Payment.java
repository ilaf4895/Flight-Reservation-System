package model;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

public class Payment {
    private String paymentId;
    private String reservationId;
    private double amount;
    private String cardNumber;
    private String cvv;
    private String expiryDate; // MM/YY format
    private String status; // SUCCESS, FAILED, PENDING, REFUNDED
    private LocalDateTime transactionDate;

    public Payment(String paymentId, String reservationId, double amount,
                   String cardNumber, String cvv, String expiryDate) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (!isValidCVV(cvv)) {
            throw new IllegalArgumentException("Invalid CVV");
        }
        if (!isValidExpiryDate(expiryDate)) {
            throw new IllegalArgumentException("Invalid expiry date");
        }

        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.cardNumber = maskCardNumber(cardNumber);
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.status = "PENDING";
        this.transactionDate = LocalDateTime.now();
    }

    public String getPaymentId() { return paymentId; }
    public String getReservationId() { return reservationId; }
    public double getAmount() { return amount; }
    public String getCardNumber() { return cardNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }

    /**
     * Validates credit card number using Luhn algorithm
     * Supports Visa, Mastercard, American Express, Discover, etc.
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-digit characters (spaces, dashes, etc.)
        String digits = cardNumber.replaceAll("\\D", "");
        
        // Check length (13-19 digits for most cards)
        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }
        
        // Apply Luhn algorithm
        return luhnCheck(digits);
    }

    /**
     * Luhn Algorithm Implementation
     * Used to validate credit card numbers
     */
    private static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Process digits from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                // If result is two digits, add them together
                if (digit > 9) {
                    digit = (digit / 10) + (digit % 10);
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        // Valid if sum is divisible by 10
        return (sum % 10) == 0;
    }

    /**
     * Validates CVV (Card Verification Value)
     * Must be 3 digits for Visa/MC or 4 digits for Amex
     */
    public static boolean isValidCVV(String cvv) {
        if (cvv == null) {
            return false;
        }
        // CVV must be 3 or 4 digits only
        return cvv.matches("^\\d{3,4}$");
    }

    /**
     * Validates expiry date in MM/YY format
     * Card must not be expired
     */
    public static boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convert YY to YYYY
            
            // Create YearMonth for comparison
            YearMonth cardExpiry = YearMonth.of(year, month);
            YearMonth currentYearMonth = YearMonth.now();
            
            // Card must not be expired (same month or future)
            return cardExpiry.compareTo(currentYearMonth) >= 0;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Masks card number for security
     * Shows first 4 and last 4 digits only
     */
    private static String maskCardNumber(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() < 8) {
            return "****";
        }
        return digits.substring(0, 4) + "****" + digits.substring(digits.length() - 4);
    }

    public void processPayment() {
        this.status = "SUCCESS";
    }

    public void failPayment() {
        this.status = "FAILED";
    }

    public void refund() {
        if (!"SUCCESS".equals(status)) {
            throw new IllegalStateException("Cannot refund unsuccessful payment");
        }
        this.status = "REFUNDED";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}