<#import "../layout.ftl" as layout>

<@layout.page title="Le mie richieste">

    <p class="actions">
        <a href="${contextPath}/orderer/request/new" class="btn btn-primary">
            Nuova richiesta di acquisto
        </a>
    </p>

    <#if requests?size == 0>
    <p>Non hai ancora effettuato richieste di acquisto.</p>
    <#else>
        <div class="table-wrap">
        <table class="table">
            <thead>
                <tr>
                    <th>N.</th>
                    <th>Richiesta</th>
                    <th>Stato</th>
                    <th>Data</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <#list requests as r>
                <tr<#if r.highlightedForOrderer> class="row-highlight"</#if>>
                    <td>#${r.id}</td>
                    <td>
                        <strong>${r.displayTitle}</strong>
                        <small>${r.category.path}</small>
                    </td>
                    <td>
                        <span class="badge badge-${r.status}">${r.status.label}</span>
                        <small>${r.status.description}</small>
                    </td>
                    <td>${r.createdAt.format('dd/MM/yyyy HH:mm')}</td>
                    <td>
                        <a href="${contextPath}/orderer/request/${r.id}">Dettagli</a>
                    </td>
                </tr>
                </#list>
            </tbody>
        </table>
        </div>
    </#if>

</@layout.page>