/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.pio.webmarket.service;

/**
 *
 * @author Pio
 */

import it.univaq.pio.webmarket.model.PurchaseProposal;
import it.univaq.pio.webmarket.model.PurchaseRequest;
import it.univaq.pio.webmarket.model.User;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notifiche via email per le transizioni di stato delle richieste.
 *
 * La specifica prevede che i passaggi siano notificati ai soggetti
 * coinvolti oltre che evidenziati nelle dashboard.
 *
 * L'invio avviene su un thread separato: una connessione SMTP puo'
 * richiedere diversi secondi e non deve bloccare la risposta HTTP
 * all'utente che ha compiuto l'azione.
 *
 * Se mail.enabled e' false i messaggi vengono solo registrati nel log,
 * cosi' l'applicazione resta utilizzabile senza un server SMTP.
 */
public class MailService {

    private static final Logger LOG = Logger.getLogger(MailService.class.getName());

    private final boolean enabled;
    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String from;
    private final String baseUrl;

    public MailService(ServletContext context) {
        this.enabled = Boolean.parseBoolean(context.getInitParameter("mail.enabled"));
        this.host = context.getInitParameter("mail.smtp.host");
        this.port = context.getInitParameter("mail.smtp.port");
        this.user = context.getInitParameter("mail.smtp.user");
        this.password = context.getInitParameter("mail.smtp.password");
        this.from = context.getInitParameter("mail.from");
        this.baseUrl = context.getInitParameter("baseUrl");
    }

    // ===== NOTIFICHE DEL FLUSSO =====

    /** Passo 1: nuova richiesta inserita, i tecnici possono prenderla in carico. */
    public void newRequest(PurchaseRequest request, List<User> technicians) {
        for (User technician : technicians) {
            send(technician.getEmail(),
                    "Nuova richiesta di acquisto #" + request.getId(),
                    "Gentile " + technician.getUsername() + ",\n\n"
                  + "e' stata inserita una nuova richiesta di acquisto "
                  + "nella categoria " + request.getCategory().getPath() + ".\n\n"
                  + link("/technician/dashboard"));
        }
    }

    /** Passo 2: un tecnico ha preso in carico la richiesta. */
    public void requestAssigned(PurchaseRequest request) {
        User orderer = request.getOrderer();
        send(orderer.getEmail(),
                "Richiesta \"" + request.getDisplayTitle() + "\" presa in carico",
                "Gentile " + orderer.getUsername() + ",\n\n"
              + "la sua richiesta di acquisto #" + request.getId()
              + " (" + request.getCategory().getPath() + ") e' stata presa "
              + "in carico da " + request.getAssignedTechnician().getUsername() + ".\n\n"
              + "Ricevera' a breve una proposta di acquisto.\n\n"
              + link("/orderer/request/" + request.getId()));
    }

    /** Passi 2 e 3: il tecnico ha inviato una proposta. */
    public void proposalSubmitted(PurchaseRequest request, PurchaseProposal proposal) {
        User orderer = request.getOrderer();
        send(orderer.getEmail(),
                "Nuova proposta per la richiesta #" + request.getId(),
                "Gentile " + orderer.getUsername() + ",\n\n"
              + "e' disponibile una proposta di acquisto per la sua richiesta #"
              + request.getId() + ":\n\n"
              + proposal.getManufacturer() + " " + proposal.getProductName() + "\n"
              + "Codice: " + proposal.getProductCode() + "\n"
              + "Prezzo: euro " + proposal.getPrice() + "\n\n"
              + "Puo' accettarla oppure respingerla indicandone il motivo.\n\n"
              + link("/orderer/request/" + request.getId()));
    }

    /** Passo 3: l'ordinante ha accettato la proposta. */
        public void proposalAccepted(PurchaseRequest request) {
        User technician = request.getAssignedTechnician();
        send(technician.getEmail(),
                "Proposta accettata per la richiesta #" + request.getId(),
                "Gentile " + technician.getUsername() + ",\n\n"
              + "l'ordinante ha accettato la proposta per la richiesta #"
              + request.getId() + ".\n\n"
              + "Puo' procedere con l'ordine di acquisto e marcare "
              + "successivamente la richiesta come ordinata.\n\n"
              + (request.getDeliveryAddress() != null
                    ? "Luogo di consegna: " + request.getDeliveryAddress() + "\n\n" : "")
              + link("/technician/request/" + request.getId()));
    }

    /** Passo 3: l'ordinante ha respinto la proposta. */
    public void proposalRejected(PurchaseRequest request, String reason) {
        User technician = request.getAssignedTechnician();
        send(technician.getEmail(),
                "Proposta respinta per la richiesta #" + request.getId(),
                "Gentile " + technician.getUsername() + ",\n\n"
              + "l'ordinante ha respinto la proposta per la richiesta #"
              + request.getId() + " con la seguente motivazione:\n\n"
              + reason + "\n\n"
              + "La richiesta e' tornata in lavorazione: puo' formulare "
              + "una nuova proposta.\n\n"
              + link("/technician/request/" + request.getId()));
    }

    /** Passo 4: il tecnico ha effettuato l'ordine. */
    public void requestOrdered(PurchaseRequest request) {
        User orderer = request.getOrderer();
        send(orderer.getEmail(),
                "Richiesta #" + request.getId() + " ordinata",
                "Gentile " + orderer.getUsername() + ",\n\n"
              + "il prodotto relativo alla sua richiesta #" + request.getId()
              + " e' stato ordinato.\n\n"
              + "Alla consegna potra' chiudere la richiesta indicando "
              + "l'esito della fornitura.\n\n"
              + link("/orderer/request/" + request.getId()));
    }

    /** Passo 5: l'ordinante ha chiuso la richiesta. */
    public void requestClosed(PurchaseRequest request) {
        User technician = request.getAssignedTechnician();
        if (technician == null) {
            return;
        }

        String notes = request.getClosingNotes();

        send(technician.getEmail(),
                "Richiesta #" + request.getId() + " chiusa",
                "Gentile " + technician.getUsername() + ",\n\n"
              + "l'ordinante ha chiuso la richiesta #" + request.getId()
              + " con il seguente esito: "
              + request.getStatus().getDescription() + ".\n\n"
              + (notes != null ? "Note: " + notes + "\n\n" : "")
              + link("/technician/request/" + request.getId()));
    }

    /** L'ordinante ha annullato la richiesta. */
    public void requestCancelled(PurchaseRequest request) {
        User technician = request.getAssignedTechnician();
        if (technician == null) {
            // Nessun tecnico l'aveva presa in carico: non c'e' nessuno da avvisare
            return;
        }

        String reason = request.getClosingNotes();

        send(technician.getEmail(),
                "Richiesta #" + request.getId() + " annullata",
                "Gentile " + technician.getUsername() + ",\n\n"
              + "l'ordinante ha annullato la richiesta #" + request.getId()
              + ". Non e' necessario procedere oltre.\n\n"
              + (reason != null ? "Motivazione: " + reason + "\n\n" : "")
              + link("/technician/request/" + request.getId()));
    }

    // ===== INVIO =====

    /**
     * Accoda l'invio di un messaggio. Il metodo ritorna immediatamente:
     * l'invio effettivo avviene in background.
     */
    private void send(String to, String subject, String body) {
        if (!enabled) {
            LOG.log(Level.INFO, "[EMAIL non inviata]\nA: {0}\nOggetto: {1}\n{2}",
                    new Object[]{to, subject, body});
            return;
        }

        Thread sender = new Thread(() -> doSend(to, subject, body));
        sender.setDaemon(true);
        sender.start();
    }

    private void doSend(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setText(body, StandardCharsets.UTF_8.name());

            Transport.send(message);
            LOG.log(Level.INFO, "Email inviata a {0}", to);

        } catch (MessagingException e) {
            // Il fallimento dell'invio non deve compromettere l'operazione
            // gia' completata sul database
            LOG.log(Level.WARNING, "Invio email fallito verso " + to, e);
        }
    }

    private String link(String path) {
        return "Puo' consultare i dettagli al seguente indirizzo:\n"
             + baseUrl + path + "\n";
    }
}