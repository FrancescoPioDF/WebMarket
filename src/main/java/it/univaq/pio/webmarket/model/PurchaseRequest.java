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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Richiesta di acquisto: l'entita' centrale del sistema.
 * Il campo status ne governa l'intero ciclo di vita.
 *
 * E' l'inverse side delle relazioni con RequestCharacteristic
 * e PurchaseProposal: delega gli aggiornamenti ai rispettivi owning side.
 */
@Entity
@Table(name = "PurchaseRequests")
@Access(AccessType.FIELD)
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User orderer;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category category;
    
    @Column(length = 150)
    private String title;
    
    @Column(length = 255)
    private String deliveryAddress;

    /** Null finche' nessun tecnico ha preso in carico la richiesta. */
    @ManyToOne(fetch = FetchType.EAGER)
    private User assignedTechnician;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestCharacteristic> characteristics = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<PurchaseProposal> purchaseProposals = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    /** Note libere per caratteristiche peculiari non previste tra quelle standard. */
    @Lob
    private String notes;

    /** Note inserite dall'ordinante alla chiusura della richiesta. */
    @Lob
    private String closingNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    /**
     * Locking ottimistico. Gestito automaticamente da Hibernate: impedisce
     * che due tecnici prendano in carico la stessa richiesta simultaneamente.
     */
    @Version
    private Integer version;

    public enum Status {
        PENDING("Aperta", "In attesa di presa in carico"),
        IN_PROGRESS("In lavorazione", "In attesa della proposta del tecnico"),
        UNDER_REVIEW("In revisione", "Proposta da valutare"),
        APPROVED("Approvata", "In attesa dell'ordine"),
        ORDERED("Ordinata", "In attesa di consegna"),
        COMPLETED("Completata", "Prodotto ricevuto e accettato"),
        REJECTED_NOT_CONFORMING("Respinta", "Prodotto non conforme"),
        REJECTED_NOT_WORKING("Respinta", "Prodotto non funzionante"),
        CANCELLED("Annullata", "Annullata dall'ordinante");

        private final String label;
        private final String description;

        Status(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        /** True se la richiesta e' ancora in lavorazione. */
        public boolean isActive() {
            return this != COMPLETED
                && this != REJECTED_NOT_CONFORMING
                && this != REJECTED_NOT_WORKING
                && this != CANCELLED;
        }
        
        public boolean isCancellable() {
            return this == PENDING
                || this == IN_PROGRESS
                || this == UNDER_REVIEW
                || this == APPROVED;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Status.PENDING;
    }

    public PurchaseRequest() {
    }

    public PurchaseRequest(User orderer, Category category) {
        this.orderer = orderer;
        this.category = category;
    }

    /** Proposta attualmente in attesa di risposta dall'ordinante. */
    @Transient
    public PurchaseProposal getCurrentProposal() {
        return purchaseProposals.stream()
                .filter(p -> p.getStatus() == PurchaseProposal.Status.PENDING)
                .findFirst()
                .orElse(null);
    }

    /** Proposta accettata dall'ordinante, se esiste. */
    @Transient
    public PurchaseProposal getAcceptedProposal() {
        return purchaseProposals.stream()
                .filter(p -> p.getStatus() == PurchaseProposal.Status.ACCEPTED)
                .findFirst()
                .orElse(null);
    }

    /** Motivazione dell'ultimo rifiuto, da mostrare al tecnico incaricato. */
    @Transient
    public String getLastRejectionReason() {
        return purchaseProposals.stream()
                .filter(p -> p.getStatus() == PurchaseProposal.Status.REJECTED)
                .findFirst()
                .map(PurchaseProposal::getRejectionReason)
                .orElse(null);
    }

    /** Evidenziazione in dashboard: la richiesta attende un'azione dell'ordinante. */
    @Transient
    public boolean isHighlightedForOrderer() {
        return status == Status.UNDER_REVIEW || status == Status.ORDERED;
    }

    /** Evidenziazione in dashboard: la richiesta attende un'azione del tecnico. */
    @Transient
    public boolean isHighlightedForTechnician() {
        return status == Status.IN_PROGRESS || status == Status.APPROVED;
    }
    
        /** Titolo da mostrare: quello scelto dall'ordinante o il nome della categoria. */
    @Transient
    public String getDisplayTitle() {
        return (title != null && !title.isBlank()) ? title : category.getName();
    }
   

    // ----- inverse side della relazione con RequestCharacteristic -----

    public void addCharacteristic(RequestCharacteristic c) {
        if (c != null && !this.characteristics.contains(c)) {
            c.setPurchaseRequest(this);
        }
    }

    public void removeCharacteristic(RequestCharacteristic c) {
        if (c != null && this.characteristics.contains(c)) {
            c.setPurchaseRequest(null);
        }
    }

    void internalAddCharacteristic(RequestCharacteristic c) {
        this.characteristics.add(c);
    }

    void internalRemoveCharacteristic(RequestCharacteristic c) {
        this.characteristics.remove(c);
    }

    // ----- inverse side della relazione con PurchaseProposal -----

    public void addProposal(PurchaseProposal p) {
        if (p != null && !this.purchaseProposals.contains(p)) {
            p.setPurchaseRequest(this);
        }
    }

    public void removeProposal(PurchaseProposal p) {
        if (p != null && this.purchaseProposals.contains(p)) {
            p.setPurchaseRequest(null);
        }
    }

    void internalAddProposal(PurchaseProposal p) {
        this.purchaseProposals.add(p);
    }

    void internalRemoveProposal(PurchaseProposal p) {
        this.purchaseProposals.remove(p);
    }

    //getter e setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOrderer() {
        return orderer;
    }

    public void setOrderer(User orderer) {
        this.orderer = orderer;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(User assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public List<RequestCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public List<PurchaseProposal> getPurchaseProposals() {
        return purchaseProposals;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClosingNotes() {
        return closingNotes;
    }

    public void setClosingNotes(String closingNotes) {
        this.closingNotes = closingNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDeliveryAddress() {
        return title;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PurchaseRequest)) return false;
        return id != null && id.equals(((PurchaseRequest) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}