/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.CategoryDAO;
import it.univaq.pio.webmarket.model.Category;
import it.univaq.pio.webmarket.model.PurchaseRequest;
import it.univaq.pio.webmarket.model.User;
import it.univaq.pio.webmarket.service.PurchaseRequestService;
import it.univaq.pio.webmarket.service.MailService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creazione di una richiesta di acquisto (passo 1 della specifica).
 *
 * La selezione della categoria avviene navigando l'albero un livello
 * alla volta; raggiunta una foglia viene generato il form con le
 * caratteristiche proprie ed ereditate.
 *
 * L'intero flusso funziona senza JavaScript.
 */
@WebServlet(name = "NewRequestServlet", urlPatterns = {"/orderer/request/new"})
public class NewRequestServlet extends BaseServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final PurchaseRequestService requestService = new PurchaseRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long categoryId = getLongParameter(request, "categoryId");
        Map<String, Object> data = new HashMap<>();
        data.put("activeNav", "new");

        if (categoryId == null) {
            data.put("categories", categoryDAO.findFullTree());
            render("orderer/select-category.ftl", data, request, response);
            return;
        }

        Category category = categoryDAO.findByIdWithCharacteristics(categoryId);

        if (category == null) {
            handleError("Categoria non trovata", request, response);
            return;
        }

        if (!category.isLeaf()) {
            // Passo intermedio: mostra le sottocategorie
            data.put("categories", category.getChildren());
            data.put("parent", category);
            render("orderer/select-category.ftl", data, request, response);
            return;
        }

        // Foglia raggiunta: genera il form delle caratteristiche
        data.put("category", category);
        data.put("characteristics", category.getInheritedCharacteristics());
        render("orderer/request-form.ftl", data, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request);
        Long categoryId = getLongParameter(request, "categoryId");

        if (categoryId == null) {
            handleError("Categoria non specificata", request, response);
            return;
        }

        // I campi del form sono nominati value_<idCaratteristica>
        // e nopref_<idCaratteristica>
        Map<Long, String> values = new HashMap<>();
        Set<Long> noPreference = new HashSet<>();

        for (String parameterName : request.getParameterMap().keySet()) {
            if (parameterName.startsWith("value_")) {
                Long id = parseId(parameterName.substring("value_".length()));
                if (id != null) {
                    values.put(id, request.getParameter(parameterName));
                }
            } else if (parameterName.startsWith("nopref_")) {
                Long id = parseId(parameterName.substring("nopref_".length()));
                if (id != null) {
                    noPreference.add(id);
                }
            }
        }

        String notes = getStringParameter(request, "notes");

        try {
            PurchaseRequest created = requestService.create(
                    user, categoryId, getStringParameter(request, "title"), values, noPreference, notes, getStringParameter(request, "deliveryAddress"));

            setSuccess(request, "Richiesta creata correttamente.");
            redirect("/orderer/request/" + created.getId(), request, response);

        } catch (IllegalArgumentException e) {
            // Ricarica il form conservando quanto inserito
            Category category = categoryDAO.findByIdWithCharacteristics(categoryId);
            Map<String, Object> data = new HashMap<>();
            data.put("activeNav", "new");
            data.put("category", category);
            data.put("characteristics", category.getInheritedCharacteristics());
            data.put("errorMessage", e.getMessage());
            data.put("submittedValues", values);
            data.put("submittedNoPreference", noPreference);
            data.put("notes", notes != null ? notes : "");
            render("orderer/request-form.ftl", data, request, response);
            data.put("title", getStringParameter(request, "title") != null
                ? getStringParameter(request, "title") : "");
        }
    }
    @Override
    public void init() throws ServletException {
        super.init();
        requestService.setMailService(new MailService(getServletContext()));
    }

    private Long parseId(String text) {
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}