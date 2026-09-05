/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.service;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.dao.CategoryDAO;
import it.univaq.pio.webmarket.dao.PurchaseProposalDAO;
import it.univaq.pio.webmarket.dao.PurchaseRequestDAO;
import it.univaq.pio.webmarket.dao.UserDAO;
import it.univaq.pio.webmarket.model.Category;
import it.univaq.pio.webmarket.model.Characteristic;
import it.univaq.pio.webmarket.model.PurchaseProposal;
import it.univaq.pio.webmarket.model.PurchaseRequest;
import it.univaq.pio.webmarket.model.RequestCharacteristic;
import it.univaq.pio.webmarket.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logica applicativa delle richieste di acquisto.
 *
 * Contiene le regole di dominio e le transizioni di stato; l'accesso
 * ai dati e' delegato ai DAO. Le notifiche vengono inviate tramite
 * MailService, iniettato dalle servlet che hanno accesso al ServletContext.
 */
public class PurchaseRequestService {

    private final PurchaseRequestDAO requestDAO = new PurchaseRequestDAO();
    private final PurchaseProposalDAO proposalDAO = new PurchaseProposalDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();

    private MailService mailService;

    /** Impostato dalle servlet nel metodo init(). */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /** Esegue la notifica solo se il servizio e' stato configurato. */
    private void notify(Runnable action) {
        if (mailService != null) {
            action.run();
        }
    }

    // ==========================================================
    // PASSO 1 - CREAZIONE DELLA RICHIESTA (ordinante)
    // ==========================================================

    /**
     * Crea una richiesta di acquisto.
     *
     * @param values       valore scelto per ogni caratteristica, indicizzato
     *                     per id di caratteristica
     * @param noPreference id delle caratteristiche marcate come "indifferente"
     * @throws IllegalArgumentException se la categoria non e' valida o
     *         mancano caratteristiche obbligatorie
     */
    public PurchaseRequest create(User orderer, Long categoryId, String title, 
                                  Map<Long, String> values, Set<Long> noPreference,
                                  String notes, String deliveryAddress) {

        Category category = categoryDAO.findByIdWithCharacteristics(categoryId);

        if (category == null) {
            throw new IllegalArgumentException("Categoria non valida");
        }
        if (!category.isLeaf()) {
            throw new IllegalArgumentException(
                    "Occorre selezionare una categoria di dettaglio");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Il luogo di consegna e' obbligatorio");
        }

        PurchaseRequest request = new PurchaseRequest(orderer, category);
        request.setTitle(title);
        request.setNotes(notes);

        for (Characteristic characteristic : category.getInheritedCharacteristics()) {

            Long characteristicId = characteristic.getId();
            boolean indifferent = noPreference.contains(characteristicId);
            String value = values.get(characteristicId);

            if (!indifferent) {
                validate(characteristic, value);
            }

            RequestCharacteristic rc = new RequestCharacteristic(
                    characteristic,
                    indifferent ? null : value,
                    indifferent);

            request.addCharacteristic(rc);
        }

        PurchaseRequest saved = requestDAO.save(request);

        notify(() -> mailService.newRequest(saved,
                userDAO.findByRole(User.Role.TECHNICIAN)));

        return saved;
    }

    /**
     * Verifica che il valore sia compatibile con il tipo dichiarato
     * sulla caratteristica. La validazione lato server e' indispensabile:
     * quella HTML puo' essere aggirata.
     */
    private void validate(Characteristic characteristic, String value) {

        boolean empty = (value == null || value.isBlank());

        if (empty) {
            if (Boolean.TRUE.equals(characteristic.getRequired())) {
                throw new IllegalArgumentException(
                        "Il campo \"" + characteristic.getName() + "\" e' obbligatorio");
            }
            return;
        }

        switch (characteristic.getType()) {
            case INTEGER:
                try {
                    Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Il campo \"" + characteristic.getName()
                          + "\" deve contenere un numero intero");
                }
                break;

            case DECIMAL:
                try {
                    Double.parseDouble(value.trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Il campo \"" + characteristic.getName()
                          + "\" deve contenere un numero");
                }
                break;

            case BOOLEAN:
                if (!value.equals("true") && !value.equals("false")) {
                    throw new IllegalArgumentException(
                            "Valore non valido per \"" + characteristic.getName() + "\"");
                }
                break;

            case ENUMERATION:
                if (!characteristic.getAllowedValuesList().contains(value)) {
                    throw new IllegalArgumentException(
                            "Valore non ammesso per \"" + characteristic.getName() + "\"");
                }
                break;

            case TEXT:
                if (value.length() > 500) {
                    throw new IllegalArgumentException(
                            "Il campo \"" + characteristic.getName() + "\" e' troppo lungo");
                }
                break;
        }
    }

    // ==========================================================
    // CONSULTAZIONE
    // ==========================================================

    public List<PurchaseRequest> findByOrderer(Long ordererId) {
        return requestDAO.findByOrderer(ordererId);
    }

    public List<PurchaseRequest> findUnassigned() {
        return requestDAO.findUnassigned();
    }

    public List<PurchaseRequest> findByTechnician(Long technicianId) {
        return requestDAO.findByTechnician(technicianId);
    }

    /**
     * Recupera una richiesta verificando che appartenga all'ordinante indicato.
     * Impedisce di leggere le richieste altrui manipolando l'id nella URL.
     */
    public PurchaseRequest findForOrderer(Long requestId, Long ordererId) {
        PurchaseRequest request = requestDAO.findByIdComplete(requestId);
        if (request == null || !request.getOrderer().getId().equals(ordererId)) {
            return null;
        }
        return request;
    }

    /**
     * Recupera una richiesta verificando che sia assegnata al tecnico indicato.
     */
    public PurchaseRequest findForTechnician(Long requestId, Long technicianId) {
        PurchaseRequest request = requestDAO.findByIdComplete(requestId);
        if (request == null
                || request.getAssignedTechnician() == null
                || !request.getAssignedTechnician().getId().equals(technicianId)) {
            return null;
        }
        return request;
    }

    // ==========================================================
    // PASSO 2 - PRESA IN CARICO E PROPOSTA (tecnico)
    // ==========================================================

    /**
     * Presa in carico di una richiesta.
     *
     * L'operazione e' protetta dal locking ottimistico: se due tecnici
     * agiscono simultaneamente, uno solo riesce.
     *
     * @throws IllegalStateException se la richiesta non e' piu' disponibile
     */
    public void assignToTechnician(Long requestId, User technician) {

        if (!requestDAO.assignTechnician(requestId, technician)) {
            throw new IllegalStateException(
                    "Questa richiesta e' gia' stata presa in carico da un altro tecnico");
        }

        notify(() -> mailService.requestAssigned(
                requestDAO.findByIdComplete(requestId)));
    }

    /**
     * Inserisce una proposta di acquisto e porta la richiesta in revisione.
     *
     * Vale sia per la prima proposta sia per quelle successive a un rifiuto:
     * lo stato di partenza e' IN_PROGRESS in entrambi i casi.
     */
    public PurchaseProposal createProposal(Long requestId, User technician,
                                           String manufacturer, String productName,
                                           String productCode, String priceText,
                                           String productUrl, String notes) {

        PurchaseRequest request = findForTechnician(requestId, technician.getId());

        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (request.getStatus() != PurchaseRequest.Status.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Non e' possibile inserire una proposta per questa richiesta");
        }

        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("Il produttore e' obbligatorio");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Il nome del prodotto e' obbligatorio");
        }
        if (productUrl == null || productUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "L'indirizzo web del prodotto e' obbligatorio");
        }
        

        BigDecimal price = parsePrice(priceText);

        PurchaseProposal proposal = new PurchaseProposal(
                technician, manufacturer.trim(), productName.trim(),
                productCode.trim(), price, productUrl, notes);

        request.addProposal(proposal);
        request.setStatus(PurchaseRequest.Status.UNDER_REVIEW);
        requestDAO.save(request);

        notify(() -> mailService.proposalSubmitted(request, proposal));

        return proposal;
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Il prezzo e' obbligatorio");
        }
        try {
            BigDecimal price = new BigDecimal(text.trim().replace(',', '.'));
            if (price.signum() <= 0) {
                throw new IllegalArgumentException("Il prezzo deve essere maggiore di zero");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Il prezzo non e' un valore valido");
        }
    }

    // ==========================================================
    // PASSO 3 - VALUTAZIONE DELLA PROPOSTA (ordinante)
    // ==========================================================

    /**
     * L'ordinante accetta la proposta corrente.
     * La richiesta passa in attesa dell'ordine da parte del tecnico.
     */
    public void acceptProposal(Long requestId, Long ordererId) {

        PurchaseRequest request = findForOrderer(requestId, ordererId);
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (request.getStatus() != PurchaseRequest.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Non c'e' una proposta da valutare");
        }

        PurchaseProposal proposal = request.getCurrentProposal();
        if (proposal == null) {
            throw new IllegalStateException("Nessuna proposta in attesa di risposta");
        }

        proposal.setStatus(PurchaseProposal.Status.ACCEPTED);
        proposal.setAnsweredAt(LocalDateTime.now());
        request.setStatus(PurchaseRequest.Status.APPROVED);

        requestDAO.save(request);

        notify(() -> mailService.proposalAccepted(request));
    }

    /**
     * L'ordinante respinge la proposta indicandone la motivazione.
     * La richiesta torna al tecnico gia' incaricato, che potra'
     * modificare il prodotto candidato.
     */
    public void rejectProposal(Long requestId, Long ordererId, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Occorre indicare il motivo del rifiuto");
        }

        PurchaseRequest request = findForOrderer(requestId, ordererId);
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (request.getStatus() != PurchaseRequest.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Non c'e' una proposta da valutare");
        }

        PurchaseProposal proposal = request.getCurrentProposal();
        if (proposal == null) {
            throw new IllegalStateException("Nessuna proposta in attesa di risposta");
        }

        proposal.setStatus(PurchaseProposal.Status.REJECTED);
        proposal.setRejectionReason(reason.trim());
        proposal.setAnsweredAt(LocalDateTime.now());

        // Il tecnico incaricato resta lo stesso
        request.setStatus(PurchaseRequest.Status.IN_PROGRESS);

        requestDAO.save(request);

        notify(() -> mailService.proposalRejected(request, reason.trim()));
    }

    // ==========================================================
    // PASSO 4 - ORDINE (tecnico)
    // ==========================================================

    /**
     * Il tecnico marca come ordinata una richiesta con proposta accettata.
     * L'ordine vero e proprio avviene fuori dall'applicativo.
     */
    public void markAsOrdered(Long requestId, Long technicianId) {

        PurchaseRequest request = findForTechnician(requestId, technicianId);
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (request.getStatus() != PurchaseRequest.Status.APPROVED) {
            throw new IllegalStateException("La richiesta non e' in attesa di ordine");
        }

        request.setStatus(PurchaseRequest.Status.ORDERED);
        requestDAO.save(request);

        notify(() -> mailService.requestOrdered(request));
    }

    // ==========================================================
    // PASSO 5 - CHIUSURA (ordinante)
    // ==========================================================

    /**
     * L'ordinante chiude la richiesta alla consegna, indicando l'esito.
     *
     * @param outcome uno tra COMPLETED, REJECTED_NOT_CONFORMING,
     *                REJECTED_NOT_WORKING
     */
    public void closeRequest(Long requestId, Long ordererId,
                             PurchaseRequest.Status outcome, String closingNotes) {

        if (outcome == null || outcome.isActive()
                || outcome == PurchaseRequest.Status.CANCELLED) {
            throw new IllegalArgumentException("Esito non valido");
        }

        PurchaseRequest request = findForOrderer(requestId, ordererId);
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (request.getStatus() != PurchaseRequest.Status.ORDERED) {
            throw new IllegalStateException("La richiesta non e' in attesa di consegna");
        }

        // Sui rifiuti la motivazione e' indispensabile per il tecnico
        if (outcome != PurchaseRequest.Status.COMPLETED
                && (closingNotes == null || closingNotes.isBlank())) {
            throw new IllegalArgumentException(
                    "Occorre indicare il motivo per cui il prodotto viene respinto");
        }

        request.setStatus(outcome);
        request.setClosedAt(LocalDateTime.now());
        request.setClosingNotes(closingNotes != null ? closingNotes.trim() : null);

        requestDAO.save(request);

        notify(() -> mailService.requestClosed(request));
    }

    // ==========================================================
    // ANNULLAMENTO (ordinante) - funzionalita' extra
    // ==========================================================

    /**
     * L'ordinante annulla la richiesta.
     *
     * Consentito fino allo stato APPROVED incluso: una volta effettuato
     * l'ordine (ORDERED) l'acquisto e' gia' avvenuto fuori dall'applicativo
     * e la richiesta deve arrivare alla chiusura con esito.
     */
    public void cancelRequest(Long requestId, Long ordererId, String reason) {

        PurchaseRequest request = findForOrderer(requestId, ordererId);
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non trovata");
        }
        if (!request.getStatus().isCancellable()) {
            throw new IllegalStateException(
                    "Questa richiesta non puo' piu' essere annullata");
        }

        // Se esiste una proposta in attesa, viene chiusa come respinta
        PurchaseProposal pending = request.getCurrentProposal();
        if (pending != null) {
            pending.setStatus(PurchaseProposal.Status.REJECTED);
            pending.setRejectionReason("Richiesta annullata dall'ordinante");
            pending.setAnsweredAt(LocalDateTime.now());
        }

        request.setStatus(PurchaseRequest.Status.CANCELLED);
        request.setClosedAt(LocalDateTime.now());
        request.setClosingNotes(reason != null && !reason.isBlank()
                ? reason.trim() : null);

        requestDAO.save(request);

        notify(() -> mailService.requestCancelled(request));
    }
}