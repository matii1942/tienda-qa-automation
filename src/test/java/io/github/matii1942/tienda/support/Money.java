package io.github.matii1942.tienda.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Formats amounts the way the storefront shows them.
 *
 * <p>The store uses {@code toLocaleString("es-AR", { maximumFractionDigits: 2 })}: a dot for
 * thousands, a comma for decimals, and no decimals when the amount is whole. So 2599.98 is
 * "$2.599,98" and 3000 is "$3.000". The symbols are set by hand instead of trusting the
 * JVM's locale data, so the expectation does not change from one machine to another.
 */
public final class Money {

    private Money() {
    }

    public static String pesos(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat format = new DecimalFormat("#,##0.##", symbols);
        format.setParseBigDecimal(true);
        return "$" + format.format(amount);
    }
}
