/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.UserDAO;
import it.univaq.pio.webmarket.model.User;
import it.univaq.pio.webmarket.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestione degli utenti da parte dell'amministratore.
 *
 * La specifica prevede che ordinanti e tecnici siano registrati
 * dall'amministratore: non esiste autoregistrazione.
 */
@WebServlet(name = "AdminUsersServlet", urlPatterns = {"/admin/users"})
public class AdminUsersServlet extends BaseServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showUserList(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = getStringParameter(request, "email");
        String username = getStringParameter(request, "username");
        String password = request.getParameter("password");
        String roleParam = getStringParameter(request, "role");

        User.Role role = null;
        if (roleParam != null) {
            try {
                role = User.Role.valueOf(roleParam);
            } catch (IllegalArgumentException e) {
                role = null;
            }
        }

        try {
            User created = authService.register(email, username, password, role);
            setSuccess(request, "Utente " + created.getUsername() + " registrato correttamente.");
            redirect("/admin/users", request, response);

        } catch (IllegalArgumentException e) {
            // Ricarica il form conservando i dati gia' inseriti
            Map<String, Object> submitted = new HashMap<>();
            submitted.put("email", email != null ? email : "");
            submitted.put("username", username != null ? username : "");
            submitted.put("role", roleParam != null ? roleParam : "");
            showUserList(request, response, e.getMessage(), submitted);
        }
    }

    private void showUserList(HttpServletRequest request, HttpServletResponse response,
                              String errorMessage, Map<String, Object> submitted)
            throws ServletException, IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("activeNav", "users");
        data.put("users", userDAO.findAll());
        data.put("roles", User.Role.values());

        if (errorMessage != null) {
            data.put("errorMessage", errorMessage);
        }
        if (submitted != null) {
            data.put("submitted", submitted);
        }

        render("admin/users.ftl", data, request, response);
    }
}