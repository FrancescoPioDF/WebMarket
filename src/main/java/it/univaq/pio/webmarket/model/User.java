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

@Entity
@Table(name = "Users")
@Access(AccessType.FIELD)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** Hash BCrypt (60 caratteri). La password in chiaro non viene mai memorizzata. */
    @Column(nullable = false, length = 60)
    private String passwordHash;

    /** Nome visualizzato nelle dashboard e nelle notifiche. */
    @Column(nullable = false, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public enum Role {
        ORDERER("Ordinante"),
        TECHNICIAN("Tecnico"),
        ADMINISTRATOR("Amministratore");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public User() {
    }

    public User(String email, String passwordHash, String username, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.role = role;
    }

    @Transient
    public boolean isOrderer() {
        return role == Role.ORDERER;
    }

    @Transient
    public boolean isTechnician() {
        return role == Role.TECHNICIAN;
    }

    @Transient
    public boolean isAdministrator() {
        return role == Role.ADMINISTRATOR;
    }

    //getter e setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }
}