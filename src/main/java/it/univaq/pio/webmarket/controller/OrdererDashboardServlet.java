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
 * Dashboard dell'ordinante: elenco delle proprie richieste, con
 * evidenziazione di quelle che richiedono un suo intervento.
 */
@WebServlet(name = "OrdererDashboardServlet", urlPatterns = {"/orderer/dashboard"})
public class OrdererDashboardServlet extends BaseServlet {

    private final PurchaseRequestService requestService = new PurchaseRequestService();

    @Override
    public void init() throws ServletException {
        super.init();
        requestService.setMailService(new MailService(getServletContext()));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getCurrentUser(request);
        List<PurchaseRequest> requests = requestService.findByOrderer(user.getId());

        long pendingAction = requests.stream()
                .filter(PurchaseRequest::isHighlightedForOrderer)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("activeNav", "dashboard");
        data.put("requests", requests);
        data.put("pendingAction", pendingAction);

        render("orderer/dashboard.ftl", data, request, response);
    }
}