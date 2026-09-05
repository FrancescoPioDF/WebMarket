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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Punto di ingresso dell'applicazione: reindirizza ogni utente
 * alla dashboard corrispondente al proprio ruolo.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request);

        if (user == null) {
            redirect("/login", request, response);
        } else if (user.isAdministrator()) {
            redirect("/admin/users", request, response);
        } else if (user.isTechnician()) {
            redirect("/technician/dashboard", request, response);
        } else {
            redirect("/orderer/dashboard", request, response);
        }
    }
}