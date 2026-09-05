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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Proposta di acquisto inserita dal tecnico incaricato.
 * Le proposte respinte non vengono sovrascritte ma conservate,
 * cosi' da mantenere lo storico delle revisioni.
 *
 * E' l'owning side della relazione con PurchaseRequest.
 */
@Entity
@Table(name = "PurchaseProposals")
@Access(AccessType.FIELD)
public class PurchaseProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseRequest purchaseRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    private User technician;

    @Column(nullable = false, length = 150)
    private String manufacturer;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 100)
    private String productCode;
    
    @Column(nullable = false, length = 1000)
    private String productUrl;

    /** BigDecimal e non double: sui valori monetari il floating point introduce errori. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Lob
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** Motivazione fornita dall'ordinante in caso di rifiuto. */
    @Lob
    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Momento in cui l'ordinante ha accettato o respinto la proposta. */
    private LocalDateTime answeredAt;

    public enum Status {
        PENDING("In attesa di risposta"),
        ACCEPTED("Accettata"),
        REJECTED("Respinta");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Status.PENDING;
    }

    public PurchaseProposal() {
    }

    public PurchaseProposal(User technician, String manufacturer, String productName,
                            String productCode, BigDecimal price, String productUrl, String notes) {
        this.technician = technician;
        this.manufacturer = manufacturer;
        this.productName = productName;
        this.productCode = productCode;
        this.price = price;
        this.productUrl = productUrl;
        this.notes = notes;
    }

    // ----- owning side della relazione con PurchaseRequest -----

    public void setPurchaseRequest(PurchaseRequest r) {
        if (this.purchaseRequest != null) {
            this.purchaseRequest.internalRemoveProposal(this);
        }
        this.purchaseRequest = r;
        if (r != null) {
            r.internalAddProposal(this);
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

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PurchaseProposal)) return false;
        return id != null && id.equals(((PurchaseProposal) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}