package com.kukso.hy.lib.economy;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * ECS Component representing a player's wallet/balance.
 * Uses Hytale's native Entity Component System for data persistence.
 */
public class WalletComponent implements Component<EntityStore> {

    /**
     * The ComponentType for WalletComponent.
     * Initialized via reflection in EconomyManager.
     */
    public static ComponentType<EntityStore, WalletComponent> TYPE;

    private double balance;

    public WalletComponent() {
        this.balance = 0.0;
    }

    public WalletComponent(double balance) {
        this.balance = Math.max(0.0, balance);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.max(0.0, balance);
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public boolean hasEnough(double amount) {
        return this.balance >= amount;
    }

    @Override
    public WalletComponent clone() {
        return new WalletComponent(this.balance);
    }
}
