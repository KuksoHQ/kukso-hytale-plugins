package com.kukso.hy.lib.economy;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * ECS component for storing a player's balance in a specific currency.
 * One component instance per currency per player entity.
 *
 * <p>Each currency has its own ComponentType registered dynamically via CurrencyManager.
 * For example:</p>
 * <ul>
 *   <li>ComponentType&lt;EntityStore, CurrencyComponent&gt; for "kuksolib:gold"</li>
 *   <li>ComponentType&lt;EntityStore, CurrencyComponent&gt; for "kuksolib:gems"</li>
 * </ul>
 *
 * <p>The same CurrencyComponent class is reused, but different ComponentType instances
 * allow the ECS to store multiple currency balances per entity.</p>
 */
public class CurrencyComponent implements Component<EntityStore> {

    private final String currencyId;
    private double balance;

    /**
     * Creates a new currency component with zero balance.
     *
     * @param currencyId The currency identifier (e.g., "gold")
     */
    public CurrencyComponent(@Nonnull String currencyId) {
        this.currencyId = currencyId;
        this.balance = 0.0;
    }

    /**
     * Creates a new currency component.
     *
     * @param currencyId     The currency identifier (e.g., "gold")
     * @param initialBalance Starting balance (clamped to 0 minimum)
     */
    public CurrencyComponent(@Nonnull String currencyId, double initialBalance) {
        this.currencyId = currencyId;
        this.balance = Math.max(0.0, initialBalance);
    }

    /**
     * Gets the currency identifier.
     *
     * @return Currency ID (e.g., "gold", "gems")
     */
    @Nonnull
    public String getCurrencyId() {
        return currencyId;
    }

    /**
     * Gets the current balance.
     *
     * @return Current balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the balance directly.
     *
     * @param balance New balance (clamped to 0 minimum)
     */
    public void setBalance(double balance) {
        this.balance = Math.max(0.0, balance);
    }

    /**
     * Deposits an amount into this balance.
     *
     * @param amount Amount to deposit (must be positive)
     * @return true if successful
     */
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance += amount;
        return true;
    }

    /**
     * Withdraws an amount from this balance.
     *
     * @param amount Amount to withdraw (must be positive)
     * @return true if successful (sufficient funds)
     */
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        this.balance -= amount;
        return true;
    }

    /**
     * Checks if this balance has at least the specified amount.
     *
     * @param amount Amount to check
     * @return true if balance >= amount
     */
    public boolean has(double amount) {
        return balance >= amount;
    }

    @Override
    public CurrencyComponent clone() {
        return new CurrencyComponent(currencyId, balance);
    }

    @Override
    public String toString() {
        return "CurrencyComponent{currencyId='" + currencyId + "', balance=" + balance + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyComponent that = (CurrencyComponent) o;
        return Double.compare(that.balance, balance) == 0 &&
               currencyId.equals(that.currencyId);
    }

    @Override
    public int hashCode() {
        int result = currencyId.hashCode();
        long temp = Double.doubleToLongBits(balance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
