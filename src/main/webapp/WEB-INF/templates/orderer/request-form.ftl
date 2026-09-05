<#import "../layout.ftl" as layout>

<@layout.page title="Nuova richiesta">

    <p class="breadcrumb">${category.path}</p>

    <#if errorMessage??>
    <div class="message message-error">${errorMessage}</div>
    </#if>

    <form method="post" action="${contextPath}/orderer/request/new" class="form">
        <input type="hidden" name="categoryId" value="${category.id}">

        <section class="card">
            <h2>Descrizione della richiesta</h2>
            <div class="field">
                <label for="title">Titolo</label>
                <input type="text" id="title" name="title"
                       value="${title!''}" maxlength="150"
                       placeholder="es. Notebook per il laboratorio">
                <small>Facoltativo: aiuta a riconoscere la richiesta nell'elenco.</small>
            </div>

            <div class="field">
                <label for="deliveryAddress">Luogo di consegna <span class="required">*</span> </label>
                <input type="text" id="deliveryAddress" name="deliveryAddress"
                       value="${deliveryAddress!''}" maxlength="255"
                       placeholder="es. Laboratorio informatico, edificio B, stanza 12">
            </div>
        </section>

        <section class="card">
            <h2>Caratteristiche richieste</h2>

            <#if characteristics?size == 0>
            <p>Per questa categoria non sono definite caratteristiche specifiche.</p>
            </#if>

            <#list characteristics as ch>
            <#assign fieldName = "value_" + ch.id>
            <#assign noPrefName = "nopref_" + ch.id>
            <#assign submitted = (submittedValues[ch.id?string])!''>
            <#assign isNoPref = submittedNoPreference?? && submittedNoPreference?seq_contains(ch.id)>

            <div class="field field-characteristic">
                <label for="${fieldName}">
                    ${ch.label}<#if ch.required> <span class="required">*</span></#if>
                </label>

                <#switch ch.type>
                    <#case "ENUMERATION">
                        <select id="${fieldName}" name="${fieldName}">
                            <option value="">Seleziona...</option>
                            <#list ch.allowedValuesList as v>
                            <option value="${v}"<#if submitted == v> selected</#if>>${v}</option>
                            </#list>
                        </select>
                        <#break>

                    <#case "BOOLEAN">
                        <select id="${fieldName}" name="${fieldName}">
                            <option value="">Seleziona...</option>
                            <option value="true"<#if submitted == "true"> selected</#if>>Sì</option>
                            <option value="false"<#if submitted == "false"> selected</#if>>No</option>
                        </select>
                        <#break>

                    <#case "INTEGER">
                        <input type="number" step="1" id="${fieldName}"
                               name="${fieldName}" value="${submitted}">
                        <#break>

                    <#case "DECIMAL">
                        <input type="number" step="0.01" id="${fieldName}"
                               name="${fieldName}" value="${submitted}">
                        <#break>

                    <#default>
                        <input type="text" id="${fieldName}" name="${fieldName}"
                               value="${submitted}" maxlength="500">
                </#switch>

                <label class="checkbox-inline">
                    <input type="checkbox" name="${noPrefName}" value="1"
                           <#if isNoPref>checked</#if>>
                    Indifferente
                </label>
            </div>
            </#list>
        </section>

        <section class="card">
            <h2>Note aggiuntive</h2>
            <div class="field">
                <label for="notes">
                    Caratteristiche particolari non previste tra quelle sopra
                </label>
                <textarea id="notes" name="notes" rows="4">${notes!''}</textarea>
            </div>
        </section>

        <p class="actions">
            <button type="submit" class="btn btn-primary">Invia richiesta</button>
            <a href="${contextPath}/orderer/request/new">Cambia categoria</a>
        </p>
    </form>

</@layout.page>