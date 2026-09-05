<#import "layout.ftl" as layout>

<@layout.page title="Accedi">

    <#if errorMessage??>
    <div class="message message-error">${errorMessage}</div>
    </#if>

    <form method="post" action="${contextPath}/login" class="form form-narrow">

        <div class="field">
            <label for="email">Indirizzo email</label>
            <input type="email" id="email" name="email"
                   value="${email!''}" required autofocus>
        </div>

        <div class="field">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>
        </div>

        <button type="submit" class="btn btn-primary">Accedi</button>
    </form>

</@layout.page>