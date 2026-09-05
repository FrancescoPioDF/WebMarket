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
import java.util.List;
import java.util.Map;

/**
 * Dashboard del tecnico: richieste non assegnate e richieste in carico,
 * con evidenziazione di quelle che richiedono un suo intervento.
 */
@WebServlet(name = "TechnicianDashboardServlet", urlPatterns = {"/technician/dashboard"})
public class TechnicianDashboardServlet extends BaseServlet {

    private final PurchaseRequestService requestService = new PurchaseRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request);

        List<PurchaseRequest> unassigned = requestService.findUnassigned();
        List<PurchaseRequest> assigned = requestService.findByTechnician(user.getId());

        long pendingAction = assigned.stream()
                .filter(PurchaseRequest::isHighlightedForTechnician)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("activeNav", "dashboard");
        data.put("unassigned", unassigned);
        data.put("assigned", assigned);
        data.put("pendingAction", pendingAction);

        render("technician/dashboard.ftl", data, request, response);
    }

    /** Presa in carico di una richiesta. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request);
        Long requestId = getLongParameter(request, "requestId");

        if (requestId == null) {
            handleError("Richiesta non specificata", request, response);
            return;
        }

        try {
            requestService.assignToTechnician(requestId, user);
            setSuccess(request, "Richiesta #" + requestId + " presa in carico.");
            redirect("/technician/request/" + requestId, request, response);

        } catch (IllegalStateException e) {
            setError(request, e.getMessage());
            redirect("/technician/dashboard", request, response);
        }
    }
    @Override
    public void init() throws ServletException {
        super.init();
        requestService.setMailService(new MailService(getServletContext()));
    }
}