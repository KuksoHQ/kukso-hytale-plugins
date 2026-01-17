package com.kukso.hy.lib.economy;

public class Transaction {
    private final boolean success;
    private final String failureReason;
    private final double amount;

    private Transaction(boolean success, String failureReason, double amount) {
        this.success = success;
        this.failureReason = failureReason;
        this.amount = amount;
    }

    public static Transaction success(double amount) {
        return new Transaction(true, null, amount);
    }

    public static Transaction fail(String reason) {
        return new Transaction(false, reason, 0);
    }

    public boolean isSuccessful() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public double getAmount() {
        return amount;
    }
}
