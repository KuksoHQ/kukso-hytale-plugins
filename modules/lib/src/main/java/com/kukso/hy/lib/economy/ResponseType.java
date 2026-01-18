package com.kukso.hy.lib.economy;

/**
 * Response type enumeration for economy transactions.
 * Indicates the result of an economy operation.
 */
public enum ResponseType {
    /**
     * Transaction completed successfully.
     */
    SUCCESS,

    /**
     * Transaction failed due to insufficient funds, invalid amount,
     * account not found, or other business logic failure.
     */
    FAILURE,

    /**
     * Feature is not supported by this economy provider.
     * For example, bank operations when banks are not implemented.
     */
    NOT_IMPLEMENTED
}
