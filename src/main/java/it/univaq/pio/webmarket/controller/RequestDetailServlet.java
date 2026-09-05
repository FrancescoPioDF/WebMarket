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
 * Dettaglio di una richiesta di acquisto vista dall'ordinante.
 *
 * L'id e' letto dal path: /orderer/request/{id}
 */
@WebServlet(name = "RequestDetailServlet", urlPatterns = {"/orderer/request/*"})
public class RequestDetailServlet extends BaseServlet {

    private final PurchaseRequestService requestService = new PurchaseRequestService();
    private static final String ACTION_ACCEPT = "accept";
    private static final String ACTION_REJECT = "reject";
    private static final String ACTION_CLOSE = "close";
    private static final String ACTION_CANCEL = "cancel";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long requestId = extractId(request.getPathInfo());

        if (requestId == null) {
            handleError("Richiesta non specificata", request, response);
            return;
        }

        User user = getCurrentUser(request);
        PurchaseRequest purchaseRequest = requestService.findForOrderer(requestId, user.getId());

        // Stesso messaggio sia se la richiesta non esiste sia se
        // appartiene a un altro utente: non rivela quali id esistano
        if (purchaseRequest == null) {
            handleError("Richiesta non trovata", request, response);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("request", purchaseRequest);

        render("orderer/request-detail.ftl", data, request, response);
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
     /**
     * Gestisce le azioni dell'ordinante sulla richiesta:
     * accettazione, rifiuto e chiusura.
     */
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

        try {
            if (ACTION_ACCEPT.equals(action)) {
                requestService.acceptProposal(requestId, user.getId());
                setSuccess(request, "Proposta accettata. Il tecnico procedera' con l'ordine.");

            } else if (ACTION_REJECT.equals(action)) {
                requestService.rejectProposal(requestId, user.getId(),
                        getStringParameter(request, "rejectionReason"));
                setSuccess(request, "Proposta respinta. Il tecnico ne formulera' una nuova.");

            } else if (ACTION_CLOSE.equals(action)) {
                String outcomeParam = getStringParameter(request, "outcome");
                PurchaseRequest.Status outcome = parseOutcome(outcomeParam);
                requestService.closeRequest(requestId, user.getId(), outcome,
                        getStringParameter(request, "closingNotes"));
                setSuccess(request, "Richiesta chiusa.");

            } else if (ACTION_CANCEL.equals(action)) {
                requestService.cancelRequest(requestId, user.getId(),
                        getStringParameter(request, "cancelReason"));
                setSuccess(request, "Richiesta annullata.");

            } else {
                handleError("Azione non riconosciuta", request, response);
                return;
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            setError(request, e.getMessage());
        }

        redirect("/orderer/request/" + requestId, request, response);
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        requestService.setMailService(new MailService(getServletContext()));
    }

    private PurchaseRequest.Status parseOutcome(String value) {
        if (value == null) {
            return null;
        }
        try {
            return PurchaseRequest.Status.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}