<#import "../layout.ftl" as layout>

<@layout.page title="Dashboard tecnico">

    <section class="card">
        <h2>Richieste da prendere in carico (${unassigned?size})</h2>

        <#if unassigned?size == 0>
        <p>Non ci sono richieste in attesa di assegnazione.</p>
        <#else>
        <div class="table-wrap">
        <table class="table">
            <thead>
                <tr>
                    <th>N.</th>
                    <th>Richiesta</th>
                    <th>Ordinante</th>
                    <th>Data</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <#list unassigned as r>
                <tr>
                    <td>#${r.id}</td>
                    <td>
                        <strong>${r.displayTitle}</strong>
                        <small>${r.category.path}</small>
                    </td>
                    <td>${r.orderer.username}</td>
                    <td>${r.createdAt.format('dd/MM/yyyy HH:mm')}</td>
                    <td>
                        <form method="post" action="${contextPath}/technician/dashboard"
                              class="form-inline">
                            <input type="hidden" name="requestId" value="${r.id}">
                            <button type="submit" class="btn btn-primary btn-small">
                                Prendi in carico
                            </button>
                        </form>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
        </div>
        </#if>
    </section>

    <section class="card">
        <h2>Le mie richieste (${assigned?size})</h2>

        <#if assigned?size == 0>
        <p>Non hai richieste in carico.</p>
        <#else>
        <div class="table-wrap">
        <table class="table">
            <thead>
                <tr>
                    <th>N.</th>
                    <th>Richiesta</th>
                    <th>Ordinante</th>
                    <th>Stato</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <#list assigned as r>
                <tr<#if r.highlightedForTechnician> class="row-highlight"</#if>>
                    <td>#${r.id}</td>
                    <td>
                        <strong>${r.displayTitle}</strong>
                        <small>${r.category.path}</small>
                    </td>
                    <td>${r.orderer.username}</td>
                    <td>
                        <span class="badge badge-${r.status}">${r.status.label}</span>
                        <small>${r.status.description}</small>
                    </td>
                    <td>
                        <a href="${contextPath}/technician/request/${r.id}">Dettagli</a>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
        </div>
        </#if>
    </section>

</@layout.page>