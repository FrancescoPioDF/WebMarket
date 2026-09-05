/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.model;

/**
 *
 * @author Pio
 */

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Definizione di un attributo associato a una categoria (parte "Attribute"
 * del modello EAV). Descrive QUALI caratteristiche esistono per una categoria;
 * i valori scelti dall'ordinante sono in RequestCharacteristic.
 */
@Entity
@Table(name = "Characteristics")
@Access(AccessType.FIELD)
public class Characteristic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    /** Determina il controllo generato nel form e la validazione lato server. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type = Type.TEXT;

    /** Es. "GB", "pollici", "cm". Mostrata accanto al nome nell'etichetta. */
    @Column(length = 20)
    private String unitOfMeasure;

    /** Solo per type = ENUMERATION: valori separati da virgola. */
    @Lob
    private String allowedValues;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(nullable = false)
    private int sortOrder = 0;

    public enum Type {
        TEXT("Testo"),
        INTEGER("Numero intero"),
        DECIMAL("Numero decimale"),
        BOOLEAN("Si' / No"),
        ENUMERATION("Scelta multipla");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public Characteristic() {
    }

    public Characteristic(String name, Type type, String unitOfMeasure,
                          Boolean required, int sortOrder) {
        this.name = name;
        this.type = type;
        this.unitOfMeasure = unitOfMeasure;
        this.required = required;
        this.sortOrder = sortOrder;
    }

    /** Valori ammessi come lista, per generare la select nel template. */
    @Transient
    public List<String> getAllowedValuesList() {
        if (allowedValues == null || allowedValues.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(allowedValues.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    /** Etichetta con unita' di misura: "Memoria RAM (GB)". */
    @Transient
    public String getLabel() {
        return (unitOfMeasure == null || unitOfMeasure.isBlank())
                ? name
                : name + " (" + unitOfMeasure + ")";
    }

    //getter e setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category c) {
        if (this.category != null) {
            this.category.internalRemoveCharacteristic(this);
        }
        this.category = c;
        if (c != null) {
            c.internalAddCharacteristic(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Characteristic)) return false;
        return id != null && id.equals(((Characteristic) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}