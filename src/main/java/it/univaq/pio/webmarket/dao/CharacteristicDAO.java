/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.dao;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.Characteristic;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Accesso ai dati delle definizioni di caratteristica.
 */
public class CharacteristicDAO {

    public Characteristic findById(Long id) {
        return PersistentManager.inTransaction(em -> em.find(Characteristic.class, id));
    }

    /** Caratteristiche definite direttamente su una categoria. */
    public List<Characteristic> findByCategory(Long categoryId) {
        return PersistentManager.inTransaction(em -> {
            TypedQuery<Characteristic> query = em.createQuery(
                    "SELECT c FROM Characteristic c WHERE c.category.id = :categoryId "
                  + "ORDER BY c.sortOrder", Characteristic.class);
            query.setParameter("categoryId", categoryId);
            return query.getResultList();
        });
    }

    public Characteristic save(Characteristic characteristic) {
        return PersistentManager.inTransaction(em -> {
            if (characteristic.getId() == null) {
                em.persist(characteristic);
                return characteristic;
            }
            return em.merge(characteristic);
        });
    }

    public void delete(Long id) {
        PersistentManager.inTransactionVoid(em -> {
            Characteristic characteristic = em.find(Characteristic.class, id);
            if (characteristic != null) {
                em.remove(characteristic);
            }
        });
    }
}