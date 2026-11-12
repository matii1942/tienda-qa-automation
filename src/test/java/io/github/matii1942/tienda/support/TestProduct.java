package io.github.matii1942.tienda.support;

import java.math.BigDecimal;

/**
 * A product created by a scenario, with a known price and stock.
 *
 * <p>Tests that buy things use their own product instead of the seeded catalogue: the seed
 * resets every product to 10 units, and a test that depended on that number would break
 * the moment another test (or a person) bought something.
 */
public record TestProduct(String id, String name, BigDecimal price, int initialStock) {
}
