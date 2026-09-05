<#import "../layout.ftl" as layout>

<@layout.page title="${request.displayTitle}">

    <p class="page-meta">Richiesta n. ${request.id} &middot; ${request.category.path}</p>

    <#if errorMessage??>
    <div class="message message-error">${errorMessage}</div>
    </#if>

    <section class="card">
        <h2>Riepilogo</h2>
        
            <#-- ===== PASSO 4: proposta accettata, in attesa di ordine ===== -->
    <#if request.status == "APPROVED">
    <section class="card card-action">
        <h2>Proposta accettata</h2>
        <p>
            L'ordinante ha accettato la proposta. Procedi con l'ordine
            di acquisto e poi marca la richiesta come ordinata.
        </p>

        <#assign accepted = request.acceptedProposal!>
        <#if accepted??>
        <dl class="detail-list">
            <dt>Prodotto da ordinare</dt>
            <dd><strong>${accepted.manufacturer} ${accepted.productName}</strong></dd>

                <#if p.productCode??>
                <dt>Codice</dt>
                <dd>${p.productCode}</dd>
                </#if>

            <dt>Prezzo</dt>
            <dd>&euro; ${accepted.price}</dd>
        </dl>
        </#if>

        <form method="post" action="${contextPath}/technician/request/${request.id}" autocomplete="off">
            <input type="hidden" name="action" value="order">
            <button type="submit" class="btn btn-primary">
                Marca come ordinata
            </button>
        </form>
    </section>
    </#if>

        <dl class="detail-list">
            <dt>Categoria</dt>
            <dd>${request.category.path}</dd>

            <dt>Ordinante</dt>
            <dd>${request.orderer.username} (${request.orderer.email})</dd>

            <dt>Stato</dt>
            <dd>
                <span class="badge badge-${request.status}">${request.status.label}</span>
                ${request.status.description}
            </dd>

            <dt>Data creazione</dt>
            <dd>${request.createdAt.format('dd/MM/yyyy HH:mm')}</dd>
        </dl>
    </section>

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
        <h3>Note dell'ordinante</h3>
        <p>${request.notes}</p>
        </#if>
    </section>

    <#-- Motivazione dell'ultimo rifiuto, se presente -->
    <#if request.lastRejectionReason??>
    <section class="card card-warning">
        <h2>Proposta precedente respinta</h2>
        <p>${request.lastRejectionReason}</p>
    </section>
    </#if>

    <#-- Form di inserimento proposta: solo se la richiesta e' in lavorazione -->
    <#if request.status == "IN_PROGRESS">
    <section class="card">
        <h2>
            <#if request.purchaseProposals?size gt 0>
            Nuova proposta di acquisto
            <#else>
            Proposta di acquisto
            </#if>
        </h2>

        <form method="post" action="${contextPath}/technician/request/${request.id}" class="form" autocomplete="off">

            <div class="field">
                <label for="manufacturer">Produttore <span class="required">*</span></label>
                <input type="text" id="manufacturer" name="manufacturer"
                       value="${(submitted.manufacturer)!''}" required maxlength="150">
            </div>

            <div class="field">
                <label for="productName">Nome prodotto <span class="required">*</span></label>
                <input type="text" id="productName" name="productName"
                       value="${(submitted.productName)!''}" required maxlength="255">
            </div>

            <div class="field">
                <label for="productCode">Codice prodotto</label>
                <input type="text" id="productCode" name="productCode"
                       value="${(submitted.productCode)!''}" required maxlength="100">
            </div>

            <div class="field">
                <label for="price">Prezzo in euro <span class="required">*</span></label>
                <input type="number" step="0.01" min="0.01" id="price" name="price"
                       value="${(submitted.price)!''}" required>
            </div>

            <div class="field">
                <label for="productUrl">Indirizzo web per approfondimenti <span class="required">*</span> </label>
                <input type="url" id="productUrl" name="productUrl"
                       value="${(submitted.productUrl)!''}" maxlength="1000">
            </div>

            <div class="field">
                <label for="notes">Note</label>
                <textarea id="notes" name="notes" rows="3">${(submitted.notes)!''}</textarea>
            </div>

            <button type="submit" class="btn btn-primary">Invia proposta all'ordinante</button>
            <input type="hidden" name="action" value="propose">
        </form>
    </section>
    </#if>

    <#-- Storico delle proposte -->
    <#if request.purchaseProposals?size gt 0>
    <section class="card">
        <h2>Proposte inviate (${request.purchaseProposals?size})</h2>

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

                <#if p.productUrl??>
                <dt>Scheda prodotto</dt>
                <dd><a href="${p.productUrl}" rel="noopener noreferrer" target="_blank">Apri</a></dd>
                </#if>

                <#if p.notes??>
                <dt>Note</dt>
                <dd>${p.notes}</dd>
                </#if>

                <dt>Inviata il</dt>
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

</@layout.page>