<#import "../layout.ftl" as layout>

<@layout.page title="${request.displayTitle}">

    <p class="page-meta">Richiesta n. ${request.id} &middot; ${request.category.path}</p>


    <section class="card">
        <h2>Riepilogo</h2>
        <dl class="detail-list">
            <dt>Categoria</dt>
            <dd>${request.category.path}</dd>

            <dt>Stato</dt>
            <dd>
                <span class="badge badge-${request.status}">${request.status.label}</span>
                ${request.status.description}
            </dd>

            <dt>Data creazione</dt>
            <dd>${request.createdAt.format('dd/MM/yyyy HH:mm')}</dd>

            <#if request.assignedTechnician??>
            <dt>Tecnico incaricato</dt>
            <dd>${request.assignedTechnician.username}</dd>
            </#if>

            <#if request.closedAt??>
            <dt>Data chiusura</dt>
            <dd>${request.closedAt.format('dd/MM/yyyy HH:mm')}</dd>
            </#if>

            <#if request.closingNotes??>
            <dt>Note di chiusura</dt>
            <dd>${request.closingNotes}</dd>
            </#if>

            <#if request.deliveryAddress??>
            <dt>Luogo di consegna</dt>
            <dd>${request.deliveryAddress}</dd>
            </#if>
        </dl>
    </section>

    <#-- ===== PASSO 3: proposta da valutare ===== -->
    <#assign current = request.currentProposal!>
    <#if current?? && request.status == "UNDER_REVIEW">
    <section class="card card-action">
        <h2>Proposta da valutare</h2>

        <dl class="detail-list">
            <dt>Prodotto</dt>
            <dd><strong>${current.manufacturer} ${current.productName}</strong></dd>

                <#if p.productCode??>
                <dt>Codice</dt>
                <dd>${p.productCode}</dd>
                </#if>

            <dt>Prezzo</dt>
            <dd>&euro; ${current.price}</dd>

            <#if current.productUrl??>
            <dt>Scheda prodotto</dt>
            <dd>
                <a href="${current.productUrl}" rel="noopener noreferrer" target="_blank">
                    Apri la scheda
                </a>
            </dd>
            </#if>

            <#if current.notes??>
            <dt>Note del tecnico</dt>
            <dd>${current.notes}</dd>
            </#if>
        </dl>

        <div class="action-group">
            <form method="post" action="${contextPath}/orderer/request/${request.id}"
                  class="form-inline"
                  data-confirm="Accettare questa proposta? Il tecnico procederà con l'ordine.">
                <input type="hidden" name="action" value="accept">
                <button type="submit" class="btn btn-primary">Accetta la proposta</button>
            </form>
        </div>

        <details class="reject-box">
            <summary>Respingi la proposta</summary>
            <form method="post" action="${contextPath}/orderer/request/${request.id}"
                  class="form"
                data-confirm="Respingere la proposta? Il tecnico dovrà formularne una nuova.">
                <input type="hidden" name="action" value="reject">
                <div class="field">
                    <label for="rejectionReason">
                        Motivo del rifiuto <span class="required">*</span>
                    </label>
                    <textarea id="rejectionReason" name="rejectionReason"
                              rows="3" required></textarea>
                    <small>
                        Il tecnico incaricato ricevera' questa motivazione
                        e formulera' una nuova proposta.
                    </small>
                </div>
                <button type="submit" class="btn btn-danger">Respingi</button>
            </form>
        </details>
    </section>
    </#if>

    <#-- ===== PASSO 5: chiusura alla consegna ===== -->
    <#if request.status == "ORDERED">
    <section class="card card-action">
        <h2>Il prodotto e' stato consegnato?</h2>
        <p>Indica l'esito della consegna per chiudere la richiesta.</p>

        <form method="post" action="${contextPath}/orderer/request/${request.id}"
              class="form" data-confirm-outcome>
            <input type="hidden" name="action" value="close">

            <div class="field">
                <label for="outcome">Esito <span class="required">*</span></label>
                <select id="outcome" name="outcome" required>
                    <option value="">Seleziona...</option>
                    <option value="COMPLETED">Prodotto accettato</option>
                    <option value="REJECTED_NOT_CONFORMING">
                        Respinto: non conforme alla richiesta
                    </option>
                    <option value="REJECTED_NOT_WORKING">
                        Respinto: non funzionante
                    </option>
                </select>
            </div>

            <div class="field">
                <label for="closingNotes">Note</label>
                <textarea id="closingNotes" name="closingNotes" rows="3"></textarea>
                <small>Obbligatorie se il prodotto viene respinto.</small>
            </div>

            <button type="submit" class="btn btn-primary">Chiudi la richiesta</button>
        </form>
    </section>
    </#if>

    <section class="card">
        <h2>Caratteristiche richieste</h2>
        <#if request.characteristics?size == 0>
        <p>Nessuna caratteristica specificata.</p>
        <#else>
        <div class="table-wrap">
        <table class="table">
            <tbody>
                <#list request.characteristics as rc>
                <tr>
                    <th>${rc.characteristic.name}</th>
                    <td>${rc.displayValue}</td>
                </tr>
                </#list>
            </tbody>
        </table>
        </div>
        </#if>

        <#if request.notes??>
        <h3>Note</h3>
        <p>${request.notes}</p>
        </#if>
    </section>

    <#-- Storico completo delle proposte ricevute -->
    <#if request.purchaseProposals?size gt 0>
    <section class="card">
        <h2>Proposte ricevute (${request.purchaseProposals?size})</h2>

        <#list request.purchaseProposals as p>
        <article class="proposal">
            <header>
                <strong>${p.manufacturer} ${p.productName}</strong>
                <span class="badge badge-proposal-${p.status}">${p.status.label}</span>
            </header>
            <dl class="detail-list">
                <#if p.productCode??>
                <dt>Codice</dt>
                <dd>${p.productCode}</dd>
                </#if>

                <dt>Prezzo</dt>
                <dd>&euro; ${p.price}</dd>

                <dt>Ricevuta il</dt>
                <dd>${p.createdAt.format('dd/MM/yyyy HH:mm')}</dd>

                <#if p.rejectionReason??>
                <dt>Motivo del rifiuto</dt>
                <dd class="error-text">${p.rejectionReason}</dd>
                </#if>
            </dl>
        </article>
        </#list>
    </section>
    </#if>

    <#-- Annullamento: consentito finche' l'ordine non e' stato effettuato -->
    <#if request.status.cancellable>
    <section class="card">
        <details class="reject-box">
            <summary>Annulla questa richiesta</summary>
            <form method="post" action="${contextPath}/orderer/request/${request.id}"
                  class="form"
                data-confirm="Annullare definitivamente questa richiesta?">
                <input type="hidden" name="action" value="cancel">

                <p>
                    L'annullamento e' definitivo: la richiesta non potra'
                    essere riattivata.
                    <#if request.assignedTechnician??>
                    Il tecnico incaricato verra' informato.
                    </#if>
                </p>

                <div class="field">
                    <label for="cancelReason">Motivazione (facoltativa)</label>
                    <textarea id="cancelReason" name="cancelReason" rows="2"></textarea>
                </div>

                <button type="submit" class="btn btn-danger">
                    Annulla definitivamente
                </button>
            </form>
        </details>
    </section>
    </#if>

</@layout.page>