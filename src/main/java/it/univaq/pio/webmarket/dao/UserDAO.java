/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.dao;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.User;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Accesso ai dati degli utenti.
 *
 * I DAO contengono esclusivamente query e operazioni di persistenza:
 * la logica applicativa risiede nel livello service.
 */
public class UserDAO {

    /**
     * Cerca un utente dal suo indirizzo email. Usato dal login.
     * Restituisce null se non esiste.
     */
    public User findByEmail(String email) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);
            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        });
    }

    public List<User> findAll() {
        return PersistentManager.inTransaction(em ->
                em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                  .getResultList());
    }

    /** Elenco degli utenti con un dato ruolo, ordinati per username. */
    public List<User> findByRole(User.Role role) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.role = :role ORDER BY u.username", User.class);
            query.setParameter("role", role);
            return query.getResultList();
        });
    }

    /** Verifica se un'email e' gia' registrata: serve alla validazione del form. */
    public boolean existsByEmail(String email) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        });
    }

    public boolean existsByUsername(String username) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        });
    }

    public User save(User user) {
        return PersistentManager.inTransaction(em -> {
            if (user.getId() == null) {
                em.persist(user);
                return user;
            }
            return em.merge(user);
        });
    }
}
