package com.kukso.hy.lib.economy;

/**
 * Represents currency metadata defined by economy plugins.
 * Economy plugins register their currencies via CurrencyManager.register().
 * Immutable record containing all configuration for a single currency type.
 *
 * @param id              Internal identifier (e.g., "gold")
 * @param displayName     Display name for UI (e.g., "Gold")
 * @param nameSingular    Singular form (e.g., "Gold")
 * @param namePlural      Plural form (e.g., "Gold")
 * @param symbol          Currency symbol (e.g., "G")
 * @param format          Format pattern (e.g., "{amount}{symbol}")
 * @param decimalPlaces   Number of decimal places (e.g., 0)
 * @param startingBalance Initial balance for new players (e.g., 100.0)
 * @param componentId     ECS ComponentType identifier (e.g., "kuksolib:gold")
 */
public record Currency(
    String id,
    String displayName,
    String nameSingular,
    String namePlural,
    String symbol,
    String format,
    int decimalPlaces,
    double startingBalance,
    String componentId
) {
    /**
     * Creates a Currency with validation.
     */
    public Currency {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Currency id cannot be null or blank");
        }
        if (displayName == null || displayName.isBlank()) {
            displayName = id;
        }
        if (nameSingular == null || nameSingular.isBlank()) {
            nameSingular = displayName;
        }
        if (namePlural == null || namePlural.isBlank()) {
            namePlural = nameSingular;
        }
        if (symbol == null) {
            symbol = "";
        }
        if (format == null || format.isBlank()) {
            format = "{symbol}{amount}";
        }
        if (decimalPlaces < 0) {
            decimalPlaces = 0;
        }
        if (startingBalance < 0) {
            startingBalance = 0.0;
        }
        if (componentId == null || componentId.isBlank()) {
            componentId = "kuksolib:" + id;
        }
    }

    /**
     * Creates a simple currency with minimal configuration.
     *
     * @param id              Internal identifier
     * @param displayName     Display name
     * @param symbol          Currency symbol
     * @param startingBalance Initial balance
     * @return A new Currency instance
     */
    public static Currency simple(String id, String displayName, String symbol, double startingBalance) {
        return new Currency(
            id,
            displayName,
            displayName,
            displayName,
            symbol,
            "{symbol}{amount}",
            2,
            startingBalance,
            "kuksolib:" + id
        );
    }

    /**
     * Formats an amount using this currency's format pattern.
     *
     * @param amount Amount to format
     * @return Formatted string (e.g., "100G" or "50 Gems")
     */
    public String formatAmount(double amount) {
        String formatted = decimalPlaces == 0
            ? String.valueOf((long) amount)
            : String.format("%." + decimalPlaces + "f", amount);
        return format
            .replace("{amount}", formatted)
            .replace("{symbol}", symbol)
            .replace("{name}", getName(amount));
    }

    /**
     * Gets the appropriate name form based on amount.
     *
     * @param amount Amount to check
     * @return Singular or plural name
     */
    public String getName(double amount) {
        return amount == 1.0 ? nameSingular : namePlural;
    }

    @Override
    public String toString() {
        return "Currency{" +
            "id='" + id + '\'' +
            ", displayName='" + displayName + '\'' +
            ", symbol='" + symbol + '\'' +
            ", format='" + format + '\'' +
            ", decimalPlaces=" + decimalPlaces +
            ", startingBalance=" + startingBalance +
            '}';
    }
}
