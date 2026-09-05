/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Gestisce il ciclo di vita dell'EntityManagerFactory.
 *
 * Tomcat e' un container di sole servlet: non implementa Jakarta EE completo,
 * quindi non esiste un container che inietti l'EntityManager o gestisca le
 * transazioni. La persistence unit e' dichiarata RESOURCE_LOCAL e tutto il
 * ciclo di vita e' gestito qui.
 *
 * La factory e' costosa da creare ed e' thread-safe: ne esiste una sola per
 * applicazione, creata all'avvio e chiusa allo spegnimento. Gli EntityManager
 * sono invece leggeri e NON thread-safe: se ne crea uno per ogni unita' di
 * lavoro e lo si chiude subito dopo.
 */
public class JPAUtil {

    private static final String PERSISTENCE_UNIT = "webmarketPU";

    private static EntityManagerFactory emf;

    private JPAUtil() {
    }

    /** Crea la factory. Invocato una sola volta all'avvio dell'applicazione. */
    public static synchronized void init() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            init();
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /** Chiude la factory. Invocato allo spegnimento dell'applicazione. */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Esegue un'operazione che restituisce un risultato dentro una transazione,
     * occupandosi di commit, rollback e chiusura dell'EntityManager.
     */
    public static <T> T inTransaction(Function<EntityManager, T> operation) {
        EntityManager em = createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = operation.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Variante senza valore di ritorno. */
    public static void inTransactionVoid(Consumer<EntityManager> operation) {
        inTransaction(em -> {
            operation.accept(em);
            return null;
        });
    }

    /** Operazione di sola lettura: nessuna transazione necessaria. */
    public static <T> T readOnly(Function<EntityManager, T> operation) {
        EntityManager em = createEntityManager();
        try {
            return operation.apply(em);
        } finally {
            em.close();
        }
    }
}