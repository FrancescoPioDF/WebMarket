/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.User;
import it.univaq.pio.webmarket.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Autenticazione degli utenti.
 *
 * Funziona interamente lato server, senza JavaScript, come richiesto
 * dalla specifica.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Un utente gia' autenticato viene mandato alla sua dashboard
        User current = getCurrentUser(request);
        if (current != null) {
            redirectToDashboard(current, request, response);
            return;
        }

        render("login.ftl", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = getStringParameter(request, "email");
        String password = request.getParameter("password");

        User user = authService.authenticate(email, password);

        if (user == null) {
            Map<String, Object> data = new HashMap<>();
            // Messaggio volutamente generico: non rivela se sia
            // sbagliata l'email o la password
            data.put("errorMessage", "Credenziali non valide");
            data.put("email", email != null ? email : "");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            render("login.ftl", data, request, response);
            return;
        }

        // Rigenera l'identificativo di sessione dopo l'autenticazione,
        // per prevenire attacchi di session fixation
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);

        redirectToDashboard(user, request, response);
    }

    /** Ogni ruolo ha la propria pagina iniziale. */
    private void redirectToDashboard(User user, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        if (user.isAdministrator()) {
            redirect("/admin/users", request, response);
        } else if (user.isTechnician()) {
            redirect("/technician/dashboard", request, response);
        } else {
            redirect("/orderer/dashboard", request, response);
        }
    }
}