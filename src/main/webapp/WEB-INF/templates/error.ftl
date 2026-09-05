<#import "layout.ftl" as layout>

<@layout.page title="Errore">
    <p class="error-text">${errorMessage!"Si è verificato un errore."}</p>
    <p><a href="${contextPath}/">Torna alla pagina principale</a></p>
</@layout.page>