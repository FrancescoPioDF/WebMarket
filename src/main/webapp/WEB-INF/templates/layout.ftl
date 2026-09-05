<#macro page title>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} &mdash; WebMarket</title>
    <link rel="stylesheet" href="${contextPath}/css/style.css">
</head>

<body>

<header class="site-header">
    <div class="container header-inner">

        <a href="${contextPath}/home" class="logo">
            Web<span>Market</span>
        </a>

        <#if currentUser??>
        <nav class="main-nav" aria-label="Navigazione principale">
            <#if currentUser.administrator>
                <a href="${contextPath}/admin/users"
                   class="nav-link<#if activeNav?? && activeNav == 'users'> is-active</#if>">
                    Utenti
                </a>
                <a href="${contextPath}/admin/categories"
                   class="nav-link<#if activeNav?? && activeNav == 'categories'> is-active</#if>">
                    Catalogo
                </a>
            <#elseif currentUser.technician>
                <a href="${contextPath}/technician/dashboard"
                   class="nav-link<#if activeNav?? && activeNav == 'dashboard'> is-active</#if>">
                    Richieste
                </a>
            <#else>
                <a href="${contextPath}/orderer/dashboard"
                   class="nav-link<#if activeNav?? && activeNav == 'dashboard'> is-active</#if>">
                    Le mie richieste
                </a>
                <a href="${contextPath}/orderer/request/new"
                   class="nav-link<#if activeNav?? && activeNav == 'new'> is-active</#if>">
                    Nuova richiesta
                </a>
            </#if>
        </nav>

        <div class="header-user">
            <span class="user-name">${currentUser.username}</span>
            <span class="user-role">${currentUser.role.label}</span>
            <a href="${contextPath}/logout" class="logout-link">Esci</a>
        </div>
        </#if>

    </div>
</header>

<main class="container">
    <#if message??>
    <div class="message message-${message.type}">
        ${message.content}
    </div>
    </#if>

    <h1>${title}</h1>

    <#nested>
</main>

<footer class="site-footer">
    <div class="container">
        <p>WebMarket &mdash; Corso di Web Engineering</p>
    </div>
</footer>
<dialog id="confirm-dialog" class="dialog">
    <p class="dialog-message"></p>
    <div class="dialog-actions">
        <button type="button" class="btn btn-ghost dialog-cancel">Annulla</button>
        <button type="button" class="btn btn-danger dialog-confirm">Conferma</button>
    </div>
</dialog>
<script src="${contextPath}/js/app.js" defer></script>
</body>
</html>
</#macro>