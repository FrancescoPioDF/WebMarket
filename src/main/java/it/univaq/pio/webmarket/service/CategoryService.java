/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.service;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.CategoryDAO;
import it.univaq.pio.webmarket.dao.CharacteristicDAO;
import it.univaq.pio.webmarket.model.Category;
import it.univaq.pio.webmarket.model.Characteristic;
import java.util.List;

/**
 * Gestione del catalogo delle categorie e delle relative
 * caratteristiche, riservata all'amministratore.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CharacteristicDAO characteristicDAO = new CharacteristicDAO();

    public List<Category> findFullTree() {
        return categoryDAO.findFullTree();
    }

    public List<Category> findAll() {
        return categoryDAO.findAll();
    }

    public Category findById(Long id) {
        return categoryDAO.findByIdWithCharacteristics(id);
    }

    /**
     * Crea una categoria, eventualmente come figlia di un'altra.
     *
     * @param parentId null per una categoria radice
     */
    public Category createCategory(String name, String description,
                                   Long parentId, Integer sortOrder) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome della categoria e' obbligatorio");
        }

        Category category = new Category(name.trim(),
                sortOrder != null ? sortOrder : 0);
        category.setDescription(description);

        if (parentId != null) {
            Category parent = categoryDAO.findById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Categoria padre non valida");
            }
            category.setParent(parent);
        }

        return categoryDAO.save(category);
    }

    /**
     * Elimina una categoria. Il cascade rimuove anche sottocategorie
     * e caratteristiche, quindi l'operazione e' bloccata se esistono
     * richieste che vi fanno riferimento.
     */
    public void deleteCategory(Long id) {
        Category category = categoryDAO.findById(id);
        if (category == null) {
            throw new IllegalArgumentException("Categoria non trovata");
        }
        try {
            categoryDAO.delete(id);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Impossibile eliminare: esistono richieste associate "
                  + "a questa categoria o alle sue sottocategorie");
        }
    }

    /**
     * Aggiunge una caratteristica a una categoria.
     * Le sottocategorie la erediteranno automaticamente.
     */
    public Characteristic createCharacteristic(Long categoryId, String name,
                                               Characteristic.Type type,
                                               String unitOfMeasure,
                                               String allowedValues,
                                               Boolean required,
                                               Integer sortOrder) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome della caratteristica e' obbligatorio");
        }
        if (type == null) {
            throw new IllegalArgumentException("Il tipo e' obbligatorio");
        }
        if (type == Characteristic.Type.ENUMERATION
                && (allowedValues == null || allowedValues.isBlank())) {
            throw new IllegalArgumentException(
                    "Per una scelta multipla occorre elencare i valori ammessi");
        }

        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Categoria non valida");
        }

        Characteristic characteristic = new Characteristic(
                name.trim(), type,
                (unitOfMeasure != null && !unitOfMeasure.isBlank())
                        ? unitOfMeasure.trim() : null,
                Boolean.TRUE.equals(required),
                sortOrder != null ? sortOrder : 0);

        characteristic.setAllowedValues(
                (allowedValues != null && !allowedValues.isBlank())
                        ? allowedValues.trim() : null);

        characteristic.setCategory(category);

        return characteristicDAO.save(characteristic);
    }

    public void deleteCharacteristic(Long id) {
        try {
            characteristicDAO.delete(id);
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "Impossibile eliminare: esistono richieste che usano "
                  + "questa caratteristica");
        }
    }
}