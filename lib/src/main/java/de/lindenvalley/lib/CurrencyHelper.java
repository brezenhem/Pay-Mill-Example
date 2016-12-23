package de.lindenvalley.lib;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyHelper {
    private static Locale getDefaultLocale() {
        return Locale.GERMANY;
    }

    /** Get the currency number format in cents. **/
    public static String format(int amount_int) {
        NumberFormat format = NumberFormat.getCurrencyInstance(getDefaultLocale());
        return format.format(getPrice(amount_int));
    }

    /** Get the currency number format in euro. **/
    public static String format(float amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(getDefaultLocale());
        return format.format(amount);
    }

    /** Currency conversion from cents to euro 99 amount_int - 0.99 amount **/
    private static float getPrice(int amount_int) {
        return amount_int / 100f;
    }

    /** Currency conversion from euro to cents 9.9 amount - 990 amount_int **/
    public static int getPriceCents(float amount) {
        return Math.round(amount * 100);
    }
}
