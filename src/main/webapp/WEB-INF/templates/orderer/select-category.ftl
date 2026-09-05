<#import "../layout.ftl" as layout>

<#--
  Macro ricorsiva: le categorie con figli diventano un <details>
  espandibile, le foglie un link alla creazione della richiesta.
  L'espansione e' gestita nativamente dal browser, senza JavaScript.
-->
<#macro tree items>
<ul class="tree">
    <#list items as c>
    <li>
        <#if c.leaf>
            <a href="${contextPath}/orderer/request/new?categoryId=${c.id}"
               class="tree-leaf">
                <span class="tree-name">${c.name}</span>
                <#if c.description??><span class="tree-desc">${c.description}</span></#if>
                <span class="tree-go">Scegli</span>
            </a>
        <#else>
            <details class="tree-branch">
                <summary>
                    <span class="tree-name">${c.name}</span>
                    <span class="tree-count">${c.children?size}</span>
                </summary>
                <@tree c.children/>
            </details>
        </#if>
    </li>
    </#list>
</ul>
</#macro>

<@layout.page title="Scegli la categoria">

    <p class="lead">
        Espandi una categoria per vedere le sottocategorie disponibili,
        poi seleziona quella corrispondente al prodotto che ti serve.
    </p>

    <div class="card">
        <@tree categories/>
    </div>

    <p class="actions">
        <a href="${contextPath}/orderer/dashboard" class="btn btn-ghost">Annulla</a>
    </p>

</@layout.page>