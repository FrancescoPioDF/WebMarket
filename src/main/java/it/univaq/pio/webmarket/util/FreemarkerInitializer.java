/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import no.api.freemarker.java8.Java8ObjectWrapper;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Inizializza il template engine Freemarker all'avvio dell'applicazione
 * e rende la configurazione disponibile a tutte le servlet tramite
 * un attributo del ServletContext.
 *
 * I template sono caricati da /WEB-INF/templates: essendo sotto WEB-INF,
 * il container non li serve mai direttamente al client, che quindi non
 * puo' scaricarne il sorgente.
 */
public class FreemarkerInitializer implements ServletContextListener {

    public static final String CONFIG_ATTRIBUTE = "freemarker_config";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        cfg.setServletContextForTemplateLoading(sce.getServletContext(), "/WEB-INF/templates");
        cfg.setDefaultEncoding("UTF-8");

        // Necessario per stampare i tipi java.time (LocalDateTime),
        // non supportati nativamente da Freemarker 2.3
        cfg.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_32));

        // In sviluppo mostra gli errori nella pagina; alla consegna
        // conviene passare a RETHROW_HANDLER
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Evita che un valore null nel modello blocchi il rendering
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        // Formati predefiniti per date e numeri
        cfg.setDateTimeFormat("dd/MM/yyyy HH:mm");
        cfg.setDateFormat("dd/MM/yyyy");
        cfg.setNumberFormat("0.##");
        
        // Escape HTML automatico su tutte le interpolazioni ${...}:
        // previene l'iniezione di script attraverso i dati inseriti dagli utenti
        cfg.setOutputFormat(freemarker.core.HTMLOutputFormat.INSTANCE);

        sce.getServletContext().setAttribute(CONFIG_ATTRIBUTE, cfg);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}