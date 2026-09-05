/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.controller;

/**
 *
 * @author Pio
 */

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
import java.util.Map;

/**
 * Dettaglio di una richiesta vista dal tecnico incaricato,
 * con il form di inserimento della proposta di acquisto.
 */
@WebServlet(name = "TechnicianRequestServlet", urlPatterns = {"/technician/request/*"})
public class TechnicianRequestServlet extends BaseServlet {

    private final PurchaseRequestService requestService = new PurchaseRequestService();
    private static final String ACTION_PROPOSE = "propose";
    private static final String ACTION_ORDER = "order";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long requestId = extractId(request.getPathInfo());
        if (requestId == null) {
            handleError("Richiesta non specificata", request, response);
            return;
        }

        User user = getCurrentUser(request);
        PurchaseRequest purchaseRequest = requestService.findForTechnician(requestId, user.getId());

        if (purchaseRequest == null) {
            handleError("Richiesta non trovata", request, response);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("request", purchaseRequest);
        render("technician/request-detail.ftl", data, request, response);
    }

    /** Inserimento di una nuova proposta di acquisto. */
     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long requestId = extractId(request.getPathInfo());
        if (requestId == null) {
            handleError("Richiesta non specificata", request, response);
            return;
        }

        User user = getCurrentUser(request);
        String action = getStringParameter(request, "action");

        if (ACTION_ORDER.equals(action)) {
            try {
                requestService.markAsOrdered(requestId, user.getId());
                setSuccess(request, "Richiesta marcata come ordinata.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                setError(request, e.getMessage());
            }
            redirect("/technician/request/" + requestId, request, response);
            return;
        }

        // Azione predefinita: inserimento di una proposta
        try {
            requestService.createProposal(
                    requestId, user,
                    getStringParameter(request, "manufacturer"),
                    getStringParameter(request, "productName"),
                    getStringParameter(request, "productCode"),
                    getStringParameter(request, "price"),
                    getStringParameter(request, "productUrl"),
                    getStringParameter(request, "notes"));

            setSuccess(request, "Proposta inviata all'ordinante.");
            redirect("/technician/request/" + requestId, request, response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            PurchaseRequest purchaseRequest =
                    requestService.findForTechnician(requestId, user.getId());

            Map<String, Object> submitted = new HashMap<>();
            submitted.put("manufacturer", nullToEmpty(request.getParameter("manufacturer")));
            submitted.put("productName", nullToEmpty(request.getParameter("productName")));
            submitted.put("productCode", nullToEmpty(request.getParameter("productCode")));
            submitted.put("price", nullToEmpty(request.getParameter("price")));
            submitted.put("productUrl", nullToEmpty(request.getParameter("productUrl")));
            submitted.put("notes", nullToEmpty(request.getParameter("notes")));

            Map<String, Object> data = new HashMap<>();
            data.put("request", purchaseRequest);
            data.put("errorMessage", e.getMessage());
            data.put("submitted", submitted);

            render("technician/request-detail.ftl", data, request, response);
        }
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        requestService.setMailService(new MailService(getServletContext()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }
        try {
            return Long.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}