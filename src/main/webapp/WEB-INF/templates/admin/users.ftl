<#import "../layout.ftl" as layout>

<@layout.page title="Gestione utenti">

    <#if errorMessage??>
    <div class="message message-error">${errorMessage}</div>
    </#if>

    <section class="card">
        <h2>Registra un nuovo utente</h2>

        <form method="post" action="${contextPath}/admin/users" class="form" autocomplete="off">

            <div class="field">
                <label for="email">Indirizzo email</label>
                <input type="email" id="email" name="email"
                       value="${(submitted.email)!''}" required>
            </div>

            <div class="field">
                <label for="username">Nome utente</label>
                <input type="text" id="username" name="username"
                       value="${(submitted.username)!''}" required maxlength="100">
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       required minlength="8">
                <small>Almeno 8 caratteri.</small>
            </div>

            <div class="field">
                <label for="role">Ruolo</label>
                <select id="role" name="role" required>
                    <option value="">Seleziona un ruolo</option>
                    <#list roles as r>
                    <option value="${r}"
                        <#if (submitted.role)?? && submitted.role == r?string>selected</#if>>
                        ${r.label}
                    </option>
                    </#list>
                </select>
            </div>

            <button type="submit" class="btn btn-primary">Registra utente</button>
        </form>
    </section>

    <section class="card">
        <h2>Utenti registrati (${users?size})</h2>

        <#if users?size == 0>
        <p>Nessun utente registrato.</p>
        <#else>
        <div class="table-wrap">
        <table class="table">
            <thead>
                <tr>
                    <th>Nome utente</th>
                    <th>Email</th>
                    <th>Ruolo</th>
                </tr>
            </thead>
            <tbody>
                <#list users as u>
                <tr>
                    <td>${u.username}</td>
                    <td>${u.email}</td>
                    <td>${u.role.label}</td>
                </tr>
                </#list>
            </tbody>
        </table>
        </div>
        </#if>
    </section>

</@layout.page>