/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.dao;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.Hibernate;
import java.util.List;
import java.util.ArrayList;

/**
 * Accesso ai dati delle categorie.
 *
 * Le collezioni children e characteristics sono lazy: i metodi di questa
 * classe le inizializzano esplicitamente prima di chiudere l'EntityManager,
 * cosi' che le entita' restituite siano utilizzabili anche una volta detached.
 */
public class CategoryDAO {

    /** Categorie radice, cioe' il primo livello dell'albero. */
    public List<Category> findRoots() {
        return PersistentManager.inTransaction(em -> {
            List<Category> roots = em.createQuery(
                    "SELECT c FROM Category c WHERE c.parent IS NULL "
                  + "ORDER BY c.sortOrder, c.name", Category.class)
                  .getResultList();
            roots.forEach(c -> initialize(c, false));
            return roots;
        });
    }
    
    public List<Category> findFullTree() {
        return PersistentManager.inTransaction(em -> {
            List<Category> all = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.sortOrder, c.name",
                    Category.class).getResultList();

            all.forEach(c -> {
                Hibernate.initialize(c.getChildren());
                Hibernate.initialize(c.getCharacteristics());
                // getLevel() e getPath() risalgono ai padri
                Category parent = c.getParent();
                while (parent != null) {
                    Hibernate.initialize(parent);
                    parent = parent.getParent();
                }
            });

            List<Category> roots = new ArrayList<>();
            for (Category c : all) {
                if (c.getParent() == null) {
                    roots.add(c);
                }
            }
            return roots;
        });
    }

    /**
     * Categoria con le proprie caratteristiche e l'intera catena degli
     * antenati inizializzata: e' quanto serve per generare il form di
     * creazione di una richiesta.
     */
    public Category findByIdWithCharacteristics(Long id) {
        return PersistentManager.inTransaction(em -> {
            Category category = em.find(Category.class, id);
            if (category != null) {
                initialize(category, true);
            }
            return category;
        });
    }

    public Category findById(Long id) {
        return PersistentManager.inTransaction(em -> {
            Category category = em.find(Category.class, id);
            if (category != null) {
                initialize(category, false);
            }
            return category;
        });
    }

    public List<Category> findAll() {
        return PersistentManager.inTransaction(em -> {
            List<Category> categories = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.sortOrder, c.name", Category.class)
                  .getResultList();
            categories.forEach(c -> initialize(c, false));
            return categories;
        });
    }

    public Category save(Category category) {
        return PersistentManager.inTransaction(em -> {
            if (category.getId() == null) {
                em.persist(category);
                return category;
            }
            return em.merge(category);
        });
    }

    public void delete(Long id) {
        PersistentManager.inTransactionVoid(em -> {
            Category category = em.find(Category.class, id);
            if (category != null) {
                em.remove(category);
            }
        });
    }

    /**
     * Forza il caricamento delle collezioni lazy.
     *
     * @param withAncestors se true risale ai padri, inizializzandone
     *                      le caratteristiche: necessario per
     *                      getInheritedCharacteristics()
     */
    private void initialize(Category category, boolean withAncestors) {
        Hibernate.initialize(category.getCharacteristics());
        Hibernate.initialize(category.getChildren());

        // Un livello piu' in profondita': serve a isLeaf() sui figli
        for (Category child : category.getChildren()) {
            Hibernate.initialize(child.getChildren());
        }

        if (withAncestors) {
            Category parent = category.getParent();
            while (parent != null) {
                Hibernate.initialize(parent.getCharacteristics());
                parent = parent.getParent();
            }
        }
    }
}
