<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Вход в Морской бой</title>
    <link rel="stylesheet" href="css/index.css">
</head>
<body>
<div class="card">
    <div class="top-bar">
        <button type="button" class="pill-button" id="themeToggle">Тёмная тема</button>
    </div>
    <h1>Вход</h1>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null && !error.trim().isEmpty()) {
    %>
    <div class="error">
        <strong>Ошибка:</strong> <%= error %>
    </div>
    <% } %>

    <form method="post" action="login">
        <div class="grid" style="grid-template-columns: 1fr;">
            <div>
                <label for="nickname">Ник</label>
                <input type="text" id="nickname" name="nickname" required>
            </div>
            <div>
                <label for="password">Пароль</label>
                <input type="password" id="password" name="password" required>
            </div>
        </div>
        <div class="actions" style="justify-content: space-between;">
            <button type="submit">Войти</button>
            <a href="register.jsp" style="text-decoration: none; font-weight: 600; color: #38bdf8; align-self: center;">Зарегистрироваться</a>
        </div>
    </form>
</div>
<script src="js/theme.js"></script>
</body>
</html>
