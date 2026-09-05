/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

/*
 * WebMarket - miglioramenti lato client.
 *
 * Tutte le funzionalita' qui implementate sono aggiuntive
 * (progressive enhancement): l'applicazione resta pienamente
 * utilizzabile con JavaScript disattivato.
 */
(function () {
    "use strict";

    /**
     * Nel form di creazione della richiesta, spuntare "Indifferente"
     * disabilita il campo corrispondente, rendendo evidente che il
     * valore non verra' considerato.
     * La validazione resta comunque lato server.
     */
    function initNoPreference() {
        var checkboxes = document.querySelectorAll('input[name^="nopref_"]');

        Array.prototype.forEach.call(checkboxes, function (checkbox) {
            var id = checkbox.name.substring("nopref_".length);
            var field = document.getElementById("value_" + id);
            if (!field) { return; }

            function sync() {
                field.disabled = checkbox.checked;
                if (checkbox.checked) { field.value = ""; }
            }

            checkbox.addEventListener("change", sync);
            sync();
        });
    }

        /**
     * Costruisce un riquadro di conferma in pagina, sostituendo il
     * dialogo nativo del browser.
     *
     * Usa l'elemento <dialog>, standard HTML: la gestione della
     * chiusura con Esc e del focus e' fornita dal browser.
     */
    function askConfirm(message, onConfirm) {
        var dialog = document.getElementById("confirm-dialog");

        if (!dialog || typeof dialog.showModal !== "function") {
            // Browser senza supporto per <dialog>: si ripiega
            // sul dialogo nativo
            if (window.confirm(message)) { onConfirm(); }
            return;
        }

        dialog.querySelector(".dialog-message").textContent = message;

        var confirmBtn = dialog.querySelector(".dialog-confirm");
        var cancelBtn = dialog.querySelector(".dialog-cancel");

        function cleanup() {
            confirmBtn.removeEventListener("click", accept);
            cancelBtn.removeEventListener("click", refuse);
            dialog.close();
        }

        function accept() { cleanup(); onConfirm(); }
        function refuse() { cleanup(); }

        confirmBtn.addEventListener("click", accept);
        cancelBtn.addEventListener("click", refuse);

        dialog.showModal();
    }

    /**
     * Intercetta l'invio dei form marcati con data-confirm e chiede
     * conferma prima di procedere.
     */
    function initConfirm() {
        var forms = document.querySelectorAll("form[data-confirm]");

        Array.prototype.forEach.call(forms, function (form) {
            form.addEventListener("submit", function (event) {
                if (form.dataset.confirmed === "yes") { return; }
                event.preventDefault();

                askConfirm(form.getAttribute("data-confirm"), function () {
                    form.dataset.confirmed = "yes";
                    form.submit();
                });
            });
        });
    }

    /**
     * Sulla chiusura di una richiesta chiede conferma solo se l'esito
     * scelto e' un rifiuto: accettare il prodotto e' l'esito normale.
     */
    function initCloseConfirm() {
        var form = document.querySelector("form[data-confirm-outcome]");
        if (!form) { return; }

        form.addEventListener("submit", function (event) {
            if (form.dataset.confirmed === "yes") { return; }

            var select = form.querySelector('select[name="outcome"]');
            if (!select || select.value.indexOf("REJECTED") !== 0) { return; }

            event.preventDefault();

            askConfirm("Respingere definitivamente il prodotto? "
                     + "La richiesta verrà chiusa e non potrà essere riaperta.",
                function () {
                    form.dataset.confirmed = "yes";
                    form.submit();
                });
        });
    }
    
    function initBackCache() {
        window.addEventListener("pageshow", function (event) {
            if (event.persisted) {
                window.location.reload();
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initNoPreference();
        initConfirm();
        initBackCache();
        initCloseConfirm();
    });
}());
