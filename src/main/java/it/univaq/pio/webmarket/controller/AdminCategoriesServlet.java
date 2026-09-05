/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.Category;
import it.univaq.pio.webmarket.model.Characteristic;
import it.univaq.pio.webmarket.service.CategoryService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestione del catalogo da parte dell'amministratore:
 * creazione ed eliminazione di categorie e caratteristiche.
 */
@WebServlet(name = "AdminCategoriesServlet", urlPatterns = {"/admin/categories"})
public class AdminCategoriesServlet extends BaseServlet {

    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showCatalogue(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = getStringParameter(request, "action");

        try {
            if ("newCategory".equals(action)) {
                categoryService.createCategory(
                        getStringParameter(request, "name"),
                        getStringParameter(request, "description"),
                        getLongParameter(request, "parentId"),
                        getIntParameter(request, "sortOrder"));
                setSuccess(request, "Categoria creata.");

            } else if ("deleteCategory".equals(action)) {
                categoryService.deleteCategory(getLongParameter(request, "categoryId"));
                setSuccess(request, "Categoria eliminata.");

            } else if ("newCharacteristic".equals(action)) {
                categoryService.createCharacteristic(
                        getLongParameter(request, "categoryId"),
                        getStringParameter(request, "name"),
                        parseType(getStringParameter(request, "type")),
                        getStringParameter(request, "unitOfMeasure"),
                        getStringParameter(request, "allowedValues"),
                        request.getParameter("required") != null,
                        getIntParameter(request, "sortOrder"));
                setSuccess(request, "Caratteristica aggiunta.");

            } else if ("deleteCharacteristic".equals(action)) {
                categoryService.deleteCharacteristic(
                        getLongParameter(request, "characteristicId"));
                setSuccess(request, "Caratteristica eliminata.");

            } else {
                handleError("Azione non riconosciuta", request, response);
                return;
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            setError(request, e.getMessage());
        }

        redirect("/admin/categories", request, response);
    }

    private void showCatalogue(HttpServletRequest request, HttpServletResponse response,
                               String errorMessage)
            throws ServletException, IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("activeNav", "categories");
        data.put("categoryTree", categoryService.findFullTree());
        data.put("allCategories", categoryService.findAll());
        data.put("types", Characteristic.Type.values());

        if (errorMessage != null) {
            data.put("errorMessage", errorMessage);
            
        }

        render("admin/categories.ftl", data, request, response);
        
        
    }

    private Characteristic.Type parseType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Characteristic.Type.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer getIntParameter(HttpServletRequest request, String name) {
        Long value = getLongParameter(request, name);
        return value != null ? value.intValue() : null;
    }
}