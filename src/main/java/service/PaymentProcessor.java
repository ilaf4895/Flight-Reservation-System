package service;

import model.Payment;
import java.util.ArrayList;
import java.util.List;

public class PaymentProcessor {
    private List<Payment> payments;
    private int paymentCounter = 5000;

    public PaymentProcessor() {
        this.payments = new ArrayList<>();
    }

    public Payment processPayment(String reservationId, double amount,
                                  String cardNumber, String cvv, String expiryDate) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String paymentId = "PAY" + (++paymentCounter);
        Payment payment = new Payment(paymentId, reservationId, amount, cardNumber, cvv, expiryDate);
        payment.processPayment();
        payments.add(payment);
        return payment;
    }

    public boolean refundPayment(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }

        Payment payment = findPaymentById(paymentId);
        if (payment == null) {
            return false;
        }
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new IllegalStateException("Cannot refund unsuccessful payment");
        }

        payment.refund();
        return true;
    }

    public Payment findPaymentById(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be empty");
        }

        return payments.stream()
                .filter(p -> p.getPaymentId().equals(paymentId))
                .findFirst()
                .orElse(null);
    }

    public List<Payment> getPaymentsByReservation(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be empty");
        }

        List<Payment> result = new ArrayList<>();
        for (Payment payment : payments) {
            if (payment.getReservationId().equals(reservationId)) {
                result.add(payment);
            }
        }
        return result;
    }

    public int getTotalPayments() {
        return payments.size();
    }

    public double getTotalRevenue() {
        return payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public int getSuccessfulPayments() {
        return (int) payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .count();
    }

    public int getFailedPayments() {
        return (int) payments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
    }
}