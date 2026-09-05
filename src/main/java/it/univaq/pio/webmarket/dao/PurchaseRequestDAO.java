/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.dao;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.PurchaseRequest;
import it.univaq.pio.webmarket.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;
import org.hibernate.Hibernate;
import java.util.List;

/**
 * Accesso ai dati delle richieste di acquisto.
 */
public class PurchaseRequestDAO {

    /** Richiesta completa di caratteristiche e proposte, per la pagina di dettaglio. */
    public PurchaseRequest findByIdComplete(Long id) {
        return PersistentManager.inTransaction(em -> {
            PurchaseRequest request = em.find(PurchaseRequest.class, id);
            if (request != null) {
                initializeComplete(request);
            }
            return request;
        });
    }

    public PurchaseRequest findById(Long id) {
        return PersistentManager.inTransaction(em -> em.find(PurchaseRequest.class, id));
    }

    /** Richieste create da un ordinante, dalla piu' recente. */
    public List<PurchaseRequest> findByOrderer(Long ordererId) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<PurchaseRequest> query = em.createQuery(
                    "SELECT r FROM PurchaseRequest r WHERE r.orderer.id = :ordererId "
                  + "ORDER BY r.createdAt DESC", PurchaseRequest.class);
            query.setParameter("ordererId", ordererId);
            List<PurchaseRequest> requests = query.getResultList();
            requests.forEach(this::initializeProposals);
            return requests;
        });
    }

    /** Richieste aperte e non ancora prese in carico: la coda dei tecnici. */
    public List<PurchaseRequest> findUnassigned() {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<PurchaseRequest> query = em.createQuery(
                    "SELECT r FROM PurchaseRequest r WHERE r.status = :status "
                  + "AND r.assignedTechnician IS NULL "
                  + "ORDER BY r.createdAt ASC", PurchaseRequest.class);
            query.setParameter("status", PurchaseRequest.Status.PENDING);
            return query.getResultList();
        });
    }

    /** Richieste assegnate a un tecnico. */
    public List<PurchaseRequest> findByTechnician(Long technicianId) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<PurchaseRequest> query = em.createQuery(
                    "SELECT r FROM PurchaseRequest r "
                  + "WHERE r.assignedTechnician.id = :technicianId "
                  + "ORDER BY r.createdAt DESC", PurchaseRequest.class);
            query.setParameter("technicianId", technicianId);
            List<PurchaseRequest> requests = query.getResultList();
            requests.forEach(this::initializeProposals);
            return requests;
        });
    }

    public PurchaseRequest save(PurchaseRequest request) {
        return PersistentManager.inTransaction(em -> {
            if (request.getId() == null) {
                em.persist(request);
                return request;
            }
            return em.merge(request);
        });
    }

    /**
     * Presa in carico atomica di una richiesta.
     *
     * Il campo @Version protegge dalla race condition in cui due tecnici
     * agiscono simultaneamente: il secondo commit fallisce con
     * OptimisticLockException invece di sovrascrivere il primo.
     *
     * @return true se la presa in carico e' riuscita, false se la richiesta
     *         non era piu' disponibile
     */
    public boolean assignTechnician(Long requestId, User technician) {
        try {
            return PersistentManager.inTransaction(em -> {
                PurchaseRequest request = em.find(PurchaseRequest.class, requestId);
                if (request == null
                        || request.getStatus() != PurchaseRequest.Status.PENDING
                        || request.getAssignedTechnician() != null) {
                    return false;
                }
                request.setAssignedTechnician(technician);
                request.setStatus(PurchaseRequest.Status.IN_PROGRESS);
                return true;
            });
        } catch (OptimisticLockException e) {
            // un altro tecnico ha preso in carico la richiesta nel frattempo
            return false;
        }
    }

    /** Inizializza le sole proposte: serve ai metodi transient della dashboard. */
    private void initializeProposals(PurchaseRequest request) {
        Hibernate.initialize(request.getPurchaseProposals());
    }

    /** Inizializza tutte le collezioni, per la pagina di dettaglio. */
    private void initializeComplete(PurchaseRequest request) {
        Hibernate.initialize(request.getCharacteristics());
        Hibernate.initialize(request.getPurchaseProposals());
        request.getCharacteristics()
               .forEach(rc -> Hibernate.initialize(rc.getCharacteristic()));
    }
}
