/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.dao;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.PurchaseProposal;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Accesso ai dati delle proposte di acquisto.
 */
public class PurchaseProposalDAO {

    public PurchaseProposal findById(Long id) {
        return PersistentManager.inTransaction(em -> em.find(PurchaseProposal.class, id));
    }

    /** Storico completo delle proposte di una richiesta, dalla piu' recente. */
    public List<PurchaseProposal> findByRequest(Long requestId) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<PurchaseProposal> query = em.createQuery(
                    "SELECT p FROM PurchaseProposal p WHERE p.purchaseRequest.id = :requestId "
                  + "ORDER BY p.createdAt DESC", PurchaseProposal.class);
            query.setParameter("requestId", requestId);
            return query.getResultList();
        });
    }

    public PurchaseProposal save(PurchaseProposal proposal) {
        return PersistentManager.inTransaction(em -> {
            if (proposal.getId() == null) {
                em.persist(proposal);
                return proposal;
            }
            return em.merge(proposal);
        });
    }
}