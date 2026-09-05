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
import java.util.Objects;

/**
 * Valore scelto dall'ordinante per una caratteristica
 * (parte "Value" del modello EAV).
 *
 * E' l'owning side della relazione con PurchaseRequest: contiene
 * la foreign key ed e' responsabile dell'aggiornamento bidirezionale.
 */
@Entity
@Table(name = "RequestCharacteristics")
@Access(AccessType.FIELD)
public class RequestCharacteristic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    private Characteristic characteristic;

    /**
     * Opzione "indifferente", prevista dalla specifica per ogni caratteristica.
     * Distingue una scelta esplicita da un campo semplicemente non compilato.
     */
    @Column(nullable = false)
    private Boolean noPreference = false;

    /**
     * Valore memorizzato sempre come stringa: e' il compromesso del modello EAV.
     * La conversione e la validazione avvengono lato applicativo in base
     * al tipo dichiarato sulla caratteristica.
     */
    @Column(length = 500)
    private String value;

    public RequestCharacteristic() {
    }

    public RequestCharacteristic(Characteristic characteristic, String value, Boolean noPreference) {
        this.characteristic = characteristic;
        this.value = value;
        this.noPreference = noPreference;
    }

    /** Valore formattato per la visualizzazione, con unita' di misura. */
    @Transient
    public String getDisplayValue() {
        if (Boolean.TRUE.equals(noPreference)) {
            return "Indifferente";
        }
        if (value == null || value.isBlank()) {
            return "\u2014";
        }
        if (characteristic.getType() == Characteristic.Type.BOOLEAN) {
            return Boolean.parseBoolean(value) ? "Si'" : "No";
        }
        String unit = characteristic.getUnitOfMeasure();
        return (unit == null || unit.isBlank()) ? value : value + " " + unit;
    }

    // ----- owning side della relazione con PurchaseRequest -----

    public void setPurchaseRequest(PurchaseRequest r) {
        if (this.purchaseRequest != null) {
            this.purchaseRequest.internalRemoveCharacteristic(this);
        }
        this.purchaseRequest = r;
        if (r != null) {
            r.internalAddCharacteristic(this);
        }
    }

    public PurchaseRequest getPurchaseRequest() {
        return purchaseRequest;
    }

    //getter e setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(Characteristic characteristic) {
        this.characteristic = characteristic;
    }

    public boolean getNoPreference() {
        return noPreference;
    }

    public void setNoPreference(Boolean noPreference) {
        this.noPreference = noPreference;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestCharacteristic)) return false;
        return id != null && id.equals(((RequestCharacteristic) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}