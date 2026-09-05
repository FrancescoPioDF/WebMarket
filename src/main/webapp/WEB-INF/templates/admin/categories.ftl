<#import "../layout.ftl" as layout>

<#--
  Macro ricorsiva: costruisce l'albero delle categorie.
  Ogni ramo espone gli strumenti di gestione contestuali, cosi'
  che l'amministratore possa operare nel punto in cui si trova
  invece di selezionare la posizione da un elenco.
-->
<#macro tree items>
<ul class="tree">
    <#list items as c>
    <li>
        <details class="tree-branch">
            <summary>
                <span class="tree-name">${c.name}</span>
                <#if c.characteristics?size gt 0>
                <span class="tree-count">${c.characteristics?size} car.</span>
                </#if>
            </summary>

            <div class="admin-cat-body">

                <#if c.characteristics?size gt 0>
                <div class="table-wrap">
                <table class="table table-compact">
                    <tbody>
                        <#list c.characteristics as ch>
                        <tr>
                            <td>${ch.label}</td>
                            <td><small>${ch.type.label}</small></td>
                            <td><#if ch.required><small>obbligatoria</small></#if></td>
                            <td class="cell-actions">
                                <form method="post"
                                      action="${contextPath}/admin/categories"
                                      class="form-inline"
                                      data-confirm="Eliminare la caratteristica &quot;${ch.name}&quot;?">
                                    <input type="hidden" name="action"
                                           value="deleteCharacteristic">
                                    <input type="hidden" name="characteristicId"
                                           value="${ch.id}">
                                    <button type="submit" class="btn-icon"
                                            title="Elimina caratteristica">
                                        &times;
                                    </button>
                                </form>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                </div>
                <#else>
                <p class="muted">Nessuna caratteristica propria di questa categoria.</p>
                </#if>

                <div class="cat-tools">

                    <details class="tool">
                        <summary class="tool-trigger">+ Sottocategoria</summary>

                        <form method="post" action="${contextPath}/admin/categories"
                              class="form form-tool" autocomplete="off">
                            <input type="hidden" name="action" value="newCategory">
                            <input type="hidden" name="parentId" value="${c.id}">

                            <div class="field">
                                <label for="sub-name-${c.id}">
                                    Nome <span class="required">*</span>
                                </label>
                                <input type="text" id="sub-name-${c.id}" name="name"
                                       required maxlength="150">
                            </div>

                            <div class="field">
                                <label for="sub-desc-${c.id}">Descrizione</label>
                                <input type="text" id="sub-desc-${c.id}"
                                       name="description" maxlength="200">
                            </div>

                            <div class="field">
                                <label for="sub-order-${c.id}">Ordinamento</label>
                                <input type="number" id="sub-order-${c.id}"
                                       name="sortOrder" value="0">
                            </div>

                            <button type="submit" class="btn btn-primary btn-small">
                                Crea dentro "${c.name}"
                            </button>
                        </form>
                    </details>

                    <details class="tool">
                        <summary class="tool-trigger">+ Caratteristica</summary>

                        <form method="post" action="${contextPath}/admin/categories"
                              class="form form-tool" autocomplete="off">
                            <input type="hidden" name="action" value="newCharacteristic">
                            <input type="hidden" name="categoryId" value="${c.id}">

                            <div class="field">
                                <label for="ch-name-${c.id}">
                                    Nome <span class="required">*</span>
                                </label>
                                <input type="text" id="ch-name-${c.id}" name="name"
                                       required maxlength="150">
                            </div>

                            <div class="field">
                                <label for="ch-type-${c.id}">
                                    Tipo <span class="required">*</span>
                                </label>
                                <select id="ch-type-${c.id}" name="type" required>
                                    <option value="">Seleziona il tipo...</option>
                                    <#list types as t>
                                    <option value="${t}">${t.label}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="field">
                                <label for="ch-unit-${c.id}">Unità di misura</label>
                                <input type="text" id="ch-unit-${c.id}"
                                       name="unitOfMeasure" maxlength="20"
                                       placeholder="GB, cm, pollici...">
                            </div>

                            <div class="field">
                                <label for="ch-val-${c.id}">Valori ammessi</label>
                                <input type="text" id="ch-val-${c.id}"
                                       name="allowedValues"
                                       placeholder="Valore 1,Valore 2">
                                <small>Solo per "Scelta multipla", separati da virgola.</small>
                            </div>

                            <div class="field">
                                <label class="checkbox-inline">
                                    <input type="checkbox" name="required" value="1">
                                    Obbligatoria
                                </label>
                            </div>

                            <div class="field">
                                <label for="ch-ord-${c.id}">Ordinamento</label>
                                <input type="number" id="ch-ord-${c.id}"
                                       name="sortOrder" value="0">
                            </div>

                            <button type="submit" class="btn btn-primary btn-small">
                                Aggiungi a "${c.name}"
                            </button>
                        </form>
                    </details>

                    <form method="post" action="${contextPath}/admin/categories"
                          class="form-inline"
                          data-confirm="Eliminare la categoria &quot;${c.name}&quot; con tutte le sue sottocategorie e caratteristiche?">
                        <input type="hidden" name="action" value="deleteCategory">
                        <input type="hidden" name="categoryId" value="${c.id}">
                        <button type="submit" class="tool-trigger tool-danger">
                            Elimina categoria
                        </button>
                    </form>

                </div>
            </div>

            <#if c.children?size gt 0>
            <@tree c.children/>
            </#if>
        </details>
    </li>
    </#list>
</ul>
</#macro>


<@layout.page title="Catalogo delle categorie">

    <#if errorMessage??>
    <div class="message message-error">${errorMessage}</div>
    </#if>

    <p class="lead">
        Espandi una categoria per gestirne le caratteristiche e aggiungere
        sottocategorie. Le caratteristiche vengono ereditate da tutte le
        categorie sottostanti.
    </p>

    <section class="card">
        <h2>Struttura del catalogo</h2>
        <@tree categoryTree/>
    </section>

    <section class="card">
        <h2>Nuova categoria principale</h2>

        <p class="muted">
            Per aggiungere una sottocategoria usa il pulsante
            "+ Sottocategoria" all'interno del ramo corrispondente.
        </p>

        <form method="post" action="${contextPath}/admin/categories"
              class="form form-narrow" autocomplete="off">
            <input type="hidden" name="action" value="newCategory">

            <div class="field">
                <label for="root-name">Nome <span class="required">*</span></label>
                <input type="text" id="root-name" name="name"
                       required maxlength="150">
            </div>

            <div class="field">
                <label for="root-desc">Descrizione</label>
                <input type="text" id="root-desc" name="description" maxlength="200">
            </div>

            <div class="field">
                <label for="root-order">Ordinamento</label>
                <input type="number" id="root-order" name="sortOrder" value="0">
                <small>Determina la posizione tra le categorie principali.</small>
            </div>

            <button type="submit" class="btn btn-primary">
                Crea categoria principale
            </button>
        </form>
    </section>

</@layout.page>