/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.PersistentManager;
import it.univaq.pio.webmarket.model.Category;
import it.univaq.pio.webmarket.model.Characteristic;
import it.univaq.pio.webmarket.model.User;
import jakarta.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inizializza JPA all'avvio dell'applicazione e, se il database e' vuoto,
 * inserisce gli utenti e le categorie di esempio. Allo spegnimento chiude
 * l'EntityManagerFactory e deregistra i driver JDBC, evitando che restino
 * agganciati al classloader dell'applicazione (memory leak sui redeploy).
 *
 * L'inserimento dei dati e' idempotente: dal secondo avvio non fa nulla.
 */

public class DataInitializer implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // La creazione della factory innesca la generazione dello schema
            PersistentManager.getInstance();
            LOG.info("JPA inizializzato correttamente.");

            long userCount = PersistentManager.inTransaction(em ->
                em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult());

            if (userCount == 0) {
                LOG.info("Database vuoto: inserimento dei dati iniziali...");
                PersistentManager.inTransactionVoid(this::populateDatabase);
                LOG.info("Dati iniziali inseriti.");
            } else {
                LOG.log(Level.INFO, "Database gia popolato ({0} utenti).", userCount);
            }

        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Errore nell'inizializzazione di JPA", e);
            throw e;
        }
    }

        @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PersistentManager.getInstance().closeEntityManagerFactory();
        LOG.info("EntityManagerFactory chiusa.");
    }

    private void populateDatabase(EntityManager em) {

        // ---------- UTENTI (password di tutti: "password123") ----------
        String hash = PasswordUtil.hash("password123");

        em.persist(new User("admin@webmarket.it", hash, "admin", User.Role.ADMINISTRATOR));
        em.persist(new User("ordinante@webmarket.it", hash, "mbianchi", User.Role.ORDERER));
        em.persist(new User("tecnico@webmarket.it", hash, "lverdi", User.Role.TECHNICIAN));

        // ---------- ALBERO DELLE CATEGORIE ----------
        Category informatica = new Category("Informatica", 1);

        Category computer = new Category("Computer", 1);
        informatica.addChild(computer);

        Category notebook = new Category("Notebook", 1);
        computer.addChild(notebook);

        Category desktop = new Category("PC Desktop", 2);
        computer.addChild(desktop);

        Category periferiche = new Category("Periferiche", 2);
        informatica.addChild(periferiche);

        Category monitor = new Category("Monitor", 1);
        periferiche.addChild(monitor);

        Category arredamento = new Category("Arredamento", 2);

        Category scrivanie = new Category("Scrivanie", 1);
        arredamento.addChild(scrivanie);

        // ---------- CARATTERISTICHE ----------
        // Definite su "Computer": ereditate da Notebook e PC Desktop
        computer.addCharacteristic(
            new Characteristic("Memoria RAM", Characteristic.Type.INTEGER, "GB", true, 1));

        computer.addCharacteristic(
            new Characteristic("Tipo CPU", Characteristic.Type.TEXT, null, true, 2));

        computer.addCharacteristic(
            new Characteristic("Capacita disco", Characteristic.Type.INTEGER, "GB", false, 3));

        Characteristic os =
            new Characteristic("Sistema operativo", Characteristic.Type.ENUMERATION, null, false, 4);
        os.setAllowedValues("Windows 11,Linux,macOS");
        computer.addCharacteristic(os);

        // ---------- Notebook ----------
        notebook.addCharacteristic(
            new Characteristic("Dimensione schermo", Characteristic.Type.DECIMAL, "pollici", true, 1));
        notebook.addCharacteristic(
            new Characteristic("Peso massimo", Characteristic.Type.DECIMAL, "kg", false, 2));
        notebook.addCharacteristic(
            new Characteristic("Autonomia minima", Characteristic.Type.INTEGER, "ore", false, 3));

        // ---------- Monitor ----------
        monitor.addCharacteristic(
            new Characteristic("Diagonale", Characteristic.Type.DECIMAL, "pollici", true, 1));

        monitor.addCharacteristic(
            new Characteristic("Risoluzione", Characteristic.Type.TEXT, null, false, 2));

        // ---------- Scrivanie ----------
        scrivanie.addCharacteristic(
            new Characteristic("Larghezza", Characteristic.Type.INTEGER, "cm", true, 1));
        scrivanie.addCharacteristic(
            new Characteristic("Profondita", Characteristic.Type.INTEGER, "cm", true, 2));

        Characteristic material =
            new Characteristic("Materiale", Characteristic.Type.ENUMERATION, null, false, 3);
        material.setAllowedValues("Legno,Metallo,Vetro,Misto");
        scrivanie.addCharacteristic(material);

        scrivanie.addCharacteristic(
            new Characteristic("Regolabile in altezza", Characteristic.Type.BOOLEAN, null, false, 4));

        // Il cascade ALL propaga il persist a tutto il sottoalbero
        em.persist(informatica);
        em.persist(arredamento);
    }
}
