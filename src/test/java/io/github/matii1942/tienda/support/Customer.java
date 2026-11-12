package io.github.matii1942.tienda.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A store account created by the tests.
 *
 * <p>Every scenario registers its own customers with a unique email, so scenarios never
 * depend on each other or on data left behind by a previous run.
 */
public final class Customer {

    /** Meets the store's rule: 6-12 characters, a digit, a lowercase and an uppercase letter. */
    public static final String VALID_PASSWORD = "Secret123";

    private final String nombre;
    private final String apellido;
    private final int edad;
    private final String carrera;
    private final String email;
    private String password;

    /** Mongo id, known after registration. */
    private String id;

    /** Session token, known after logging in through the API. */
    private String token;

    private Customer(String nombre, String email, String password) {
        this.nombre = nombre;
        this.apellido = "Tester";
        this.edad = 30;
        this.carrera = "QA";
        this.email = email;
        this.password = password;
    }

    public static Customer random() {
        return named("Customer");
    }

    public static Customer named(String alias) {
        return new Customer(alias, uniqueEmail(alias), VALID_PASSWORD);
    }

    /** Same personal data and email as {@code other}: used to test duplicate registration. */
    public static Customer withSameEmailAs(Customer other) {
        return new Customer("Duplicate", other.email, VALID_PASSWORD);
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueEmail(String alias) {
        return "qa-" + alias.toLowerCase() + "-" + uniqueSuffix() + "@example.test";
    }

    /** The registration body the store expects (field names are the API's, in Spanish). */
    public Map<String, Object> registrationBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", nombre);
        body.put("apellido", apellido);
        body.put("edad", edad);
        body.put("carrera", carrera);
        body.put("email", email);
        if (password != null) {
            body.put("password", password);
        }
        return body;
    }

    public Customer withPassword(String newPassword) {
        this.password = newPassword;
        return this;
    }

    public String nombre() {
        return nombre;
    }

    public String apellido() {
        return apellido;
    }

    public int edad() {
        return edad;
    }

    public String carrera() {
        return carrera;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return nombre + " <" + email + ">";
    }
}
