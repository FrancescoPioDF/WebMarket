/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.univaq.pio.webmarket.model.User;
import it.univaq.pio.webmarket.util.FreemarkerInitializer;
import it.univaq.pio.webmarket.util.Message;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe base di tutte le servlet dell'applicazione.
 *
 * Fornisce il rendering dei template Freemarker, l'accesso all'utente
 * autenticato e la gestione uniforme degli errori.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    public static final String SESSION_USER = "user";

    /**
     * Renderizza un template scrivendo direttamente sulla response.
     *
     * Non usiamo FreemarkerServlet perche' e' compilata contro una specifica
     * versione della servlet API: cosi' il codice resta indipendente dal
     * container.
     */
    protected void render(String templateName, Map<String, Object> data,
                          HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Configuration cfg = (Configuration) getServletContext()
                .getAttribute(FreemarkerInitializer.CONFIG_ATTRIBUTE);

        if (cfg == null) {
            throw new ServletException("Configurazione Freemarker non inizializzata");
        }

        // Dati comuni a tutti i template
        data.put("contextPath", request.getContextPath());
        data.put("baseUrl", getServletContext().getInitParameter("baseUrl"));

        User currentUser = getCurrentUser(request);
        if (currentUser != null) {
            data.put("currentUser", currentUser);
        }

        // Messaggio di feedback eventualmente lasciato in sessione
        // da una precedente redirect
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object message = session.getAttribute("message");
            if (message != null) {
                data.put("message", message);
                session.removeAttribute("message");
            }
        }
        
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            Template template = cfg.getTemplate(templateName);
            template.process(data, out);
        } catch (TemplateException e) {
            LOG.log(Level.SEVERE, "Errore nel rendering del template " + templateName, e);
            throw new ServletException("Errore nella generazione della pagina", e);
        }
    }

    /** Variante senza dati aggiuntivi. */
    protected void render(String templateName,
                          HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(templateName, new HashMap<>(), request, response);
    }

    /** Utente attualmente autenticato, o null. */
    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SESSION_USER);
    }

    /**
     * Salva un messaggio in sessione perche' sopravviva a una redirect.
     * Verra' letto e rimosso al primo rendering successivo.
     */
    protected void setMessage(HttpServletRequest request, Message message) {
        request.getSession(true).setAttribute("message", message);
    }

    protected void setSuccess(HttpServletRequest request, String text) {
        setMessage(request, Message.success(text));
    }

    protected void setError(HttpServletRequest request, String text) {
        setMessage(request, Message.error(text));
    }

    /** Redirect verso un percorso interno all'applicazione. */
    protected void redirect(String path, HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /** Legge un parametro numerico, restituendo null se assente o malformato. */
    protected Long getLongParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Legge un parametro testuale, restituendo null se vuoto. */
    protected String getStringParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    /** Pagina di errore uniforme. */
    protected void handleError(String messageText, HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("errorMessage", messageText);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        render("error.ftl", data, request, response);
    }

    protected void handleError(Exception e, HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Errore nella gestione della richiesta", e);
        handleError("Si e' verificato un errore imprevisto.", request, response);
    }
}