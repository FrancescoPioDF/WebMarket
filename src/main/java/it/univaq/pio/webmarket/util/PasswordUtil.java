/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashing delle password con BCrypt.
 *
 * BCrypt incorpora nel risultato sia il salt sia il fattore di costo, quindi
 * non serve memorizzarli separatamente. L'algoritmo e' deliberatamente lento
 * per rendere impraticabili gli attacchi a forza bruta sugli hash sottratti.
 */
public class PasswordUtil {

    /** Fattore di costo: ogni incremento raddoppia il tempo di calcolo. */
    private static final int COST = 10;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("La password non puo' essere vuota");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // hash malformato nel database
            return false;
        }
    }
}