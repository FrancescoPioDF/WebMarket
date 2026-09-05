/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.util;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.User;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controlla l'accesso a tutte le risorse dell'applicazione.
 *
 * La specifica richiede che il sito non sia navigabile senza autenticazione:
 * un utente non autenticato vede esclusivamente la pagina di login.
 *
 * Il filtro opera su due livelli:
 *  - autenticazione: verifica che esista un utente in sessione
 *  - autorizzazione: verifica che il ruolo dell'utente sia compatibile
 *    con l'area richiesta
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // Le risorse pubbliche passano sempre: senza questa eccezione
        // il CSS della pagina di login non verrebbe mai servito
        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!isAuthorized(path, user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Non hai i permessi per accedere a questa pagina");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Percorsi accessibili senza autenticazione. */
    private boolean isPublic(String path) {
        return path.equals("/login")
            || path.equals("/logout")
            || path.startsWith("/css/")
            || path.startsWith("/js/")
            || path.startsWith("/img/");
    }

    /**
     * Verifica che il ruolo dell'utente sia compatibile con l'area richiesta.
     * Senza questo controllo, un ordinante potrebbe raggiungere la dashboard
     * del tecnico digitandone direttamente la URL.
     */
    private boolean isAuthorized(String path, User user) {
        if (path.startsWith("/admin/")) {
            return user.isAdministrator();
        }
        if (path.startsWith("/technician/")) {
            return user.isTechnician();
        }
        if (path.startsWith("/orderer/")) {
            return user.isOrderer();
        }
        // Aree comuni: basta essere autenticati
        return true;
    }

    @Override
    public void destroy() {
    }
}
