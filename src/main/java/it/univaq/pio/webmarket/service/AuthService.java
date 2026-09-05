/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.service;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.UserDAO;
import it.univaq.pio.webmarket.model.User;
import it.univaq.pio.webmarket.util.PasswordUtil;

/**
 * Logica di autenticazione e gestione degli utenti.
 *
 * Il livello service contiene le regole applicative e usa i DAO per
 * l'accesso ai dati, senza scrivere query direttamente.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Verifica le credenziali.
     *
     * @return l'utente autenticato, oppure null se le credenziali non sono valide
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());

        // La verifica viene eseguita anche quando l'utente non esiste,
        // cosi' il tempo di risposta non rivela quali email siano registrate
        if (user == null) {
            PasswordUtil.verify(password, "$2a$10$" + "0".repeat(53));
            return null;
        }

        return PasswordUtil.verify(password, user.getPasswordHash()) ? user : null;
    }

    /**
     * Registra un nuovo utente. La registrazione e' riservata
     * all'amministratore: non esiste autoregistrazione.
     *
     * @throws IllegalArgumentException se i dati non sono validi
     *         o email/username sono gia' in uso
     */
    public User register(String email, String username, String password, User.Role role) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'indirizzo email e' obbligatorio");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Il nome utente e' obbligatorio");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La password deve avere almeno 8 caratteri");
        }
        if (role == null) {
            throw new IllegalArgumentException("Il ruolo e' obbligatorio");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("L'indirizzo email non e' valido");
        }
        if (userDAO.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Questo indirizzo email e' gia' registrato");
        }
        if (userDAO.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Questo nome utente e' gia' in uso");
        }

        User user = new User(normalizedEmail, PasswordUtil.hash(password),
                             username.trim(), role);
        return userDAO.save(user);
    }
}