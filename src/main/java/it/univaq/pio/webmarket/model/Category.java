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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Categoria di prodotto. L'auto-relazione parent/children realizza l'albero
 * a profondita' libera: Informatica > Computer > Notebook.
 *
 * La relazione e' bidirezionale: parent e' l'owning side (contiene la
 * foreign key), children e' l'inverse side.
 */
@Entity
@Table(name = "Categories")
@Access(AccessType.FIELD)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, name ASC")
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Characteristic> characteristics = new ArrayList<>();

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public Category() {
    }

    public Category(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    /** Solo le foglie sono selezionabili in una richiesta di acquisto. */
    @Transient
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /** Percorso completo dalla radice: "Informatica > Computer > Notebook". */
    @Transient
    public String getPath() {
        return (parent == null) ? name : parent.getPath() + " > " + name;
    }

    /** Profondita' nell'albero: 0 per le radici. */
    @Transient
    public int getLevel() {
        return (parent == null) ? 0 : parent.getLevel() + 1;
    }

    /** Catena dalla radice fino a questa categoria inclusa. */
    @Transient
    public List<Category> getAncestorChain() {
        LinkedList<Category> chain = new LinkedList<>();
        Category current = this;
        while (current != null) {
            chain.addFirst(current);
            current = current.getParent();
        }
        return chain;
    }

    /**
     * Caratteristiche proprie piu' quelle ereditate dagli antenati:
     * e' il set che l'ordinante compila.
     */
    @Transient
    public List<Characteristic> getInheritedCharacteristics() {
        List<Characteristic> result = new ArrayList<>();
        for (Category c : getAncestorChain()) {
            result.addAll(c.getCharacteristics());
        }
        return result;
    }

    // ----- relazione bidirezionale parent/children -----
    // owning side: parent

    public void setParent(Category e) {
        if (this.parent != null) {
            this.parent.internalRemoveChild(this);
        }
        this.parent = e;
        if (e != null) {
            e.internalAddChild(this);
        }
    }

    // inverse side: children, delega all'owning side

    public void addChild(Category e) {
        if (e != null && !this.children.contains(e)) {
            e.setParent(this);
        }
    }

    public void removeChild(Category e) {
        if (e != null && this.children.contains(e)) {
            e.setParent(null);
        }
    }

    void internalAddChild(Category e) {
        this.children.add(e);
    }

    void internalRemoveChild(Category e) {
        this.children.remove(e);
    }

    // ----- relazione bidirezionale category/characteristics -----
    // owning side: Characteristic.category

    public void addCharacteristic(Characteristic e) {
        if (e != null && !this.characteristics.contains(e)) {
            e.setCategory(this);
        }
    }

    public void removeCharacteristic(Characteristic e) {
        if (e != null && this.characteristics.contains(e)) {
            e.setCategory(null);
        }
    }

    void internalAddCharacteristic(Characteristic e) {
        this.characteristics.add(e);
    }

    void internalRemoveCharacteristic(Characteristic e) {
        this.characteristics.remove(e);
    }

    //getter e setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getParent() {
        return parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        return id != null && id.equals(((Category) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}