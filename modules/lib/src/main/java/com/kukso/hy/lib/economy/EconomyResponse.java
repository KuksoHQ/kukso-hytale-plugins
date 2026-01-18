package com.kukso.hy.lib.economy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the result of an economy transaction.
 * Contains information about the transaction outcome, amounts, and any error messages.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * EconomyResponse response = economy.withdraw(player, 100.0);
 * if (response.isSuccess()) {
 *     player.sendMessage("Withdrew " + response.getAmount());
 * } else {
 *     player.sendMessage("Error: " + response.getErrorMessage());
 * }
 * }</pre>
 */
public class EconomyResponse {

    private final double amount;
    private final double balance;
    private final ResponseType type;
    private final String errorMessage;

    /**
     * Creates a new EconomyResponse.
     *
     * @param amount       The amount that was transacted
     * @param balance      The new balance after the transaction
     * @param type         The response type (SUCCESS, FAILURE, NOT_IMPLEMENTED)
     * @param errorMessage An optional error message if the transaction failed
     */
    public EconomyResponse(double amount, double balance, @Nonnull ResponseType type, @Nullable String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the amount that was transacted.
     *
     * @return The transaction amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the new balance after the transaction.
     *
     * @return The new balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Gets the response type.
     *
     * @return SUCCESS, FAILURE, or NOT_IMPLEMENTED
     */
    @Nonnull
    public ResponseType getType() {
        return type;
    }

    /**
     * Gets the error message if the transaction failed.
     *
     * @return Error message, or null if successful
     */
    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Convenience method to check if the transaction was successful.
     *
     * @return true if the transaction completed successfully
     */
    public boolean isSuccess() {
        return type == ResponseType.SUCCESS;
    }

    /**
     * Convenience method to check if the transaction failed.
     *
     * @return true if the transaction failed
     */
    public boolean isFailure() {
        return type == ResponseType.FAILURE;
    }

    /**
     * Convenience method to check if the feature is not implemented.
     *
     * @return true if the feature is not supported by the provider
     */
    public boolean isNotImplemented() {
        return type == ResponseType.NOT_IMPLEMENTED;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a successful response.
     *
     * @param amount  The amount transacted
     * @param balance The new balance
     * @return A successful EconomyResponse
     */
    public static EconomyResponse success(double amount, double balance) {
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    /**
     * Creates a successful response with a message.
     *
     * @param amount  The amount transacted
     * @param balance The new balance
     * @param message A success message
     * @return A successful EconomyResponse
     */
    public static EconomyResponse success(double amount, double balance, String message) {
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, message);
    }

    /**
     * Creates a failure response.
     *
     * @param errorMessage The error message
     * @return A failed EconomyResponse
     */
    public static EconomyResponse failure(String errorMessage) {
        return new EconomyResponse(0, 0, ResponseType.FAILURE, errorMessage);
    }

    /**
     * Creates a failure response with balance info.
     *
     * @param balance      The current balance
     * @param errorMessage The error message
     * @return A failed EconomyResponse
     */
    public static EconomyResponse failure(double balance, String errorMessage) {
        return new EconomyResponse(0, balance, ResponseType.FAILURE, errorMessage);
    }

    /**
     * Creates a not-implemented response.
     *
     * @param feature The feature that is not implemented
     * @return A not-implemented EconomyResponse
     */
    public static EconomyResponse notImplemented(String feature) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, feature + " not supported");
    }

    @Override
    public String toString() {
        return "EconomyResponse{" +
            "amount=" + amount +
            ", balance=" + balance +
            ", type=" + type +
            ", errorMessage='" + errorMessage + '\'' +
            '}';
    }
}
