<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Регистрация в Морском бое</title>
    <link rel="stylesheet" href="css/auth.css">
</head>
<body>
<div class="card">
    <div class="top-bar">
        <button type="button" class="theme-button" id="themeToggle">Тёмная тема</button>
    </div>
    <h1>Регистрация</h1>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null && !error.trim().isEmpty()) {
    %>
    <div class="error">
        <strong>Ошибка:</strong> <%= error %>
    </div>
    <% } %>

    <form method="post" action="register">
        <div class="grid">
            <div>
                <label for="nickname">Имя пользователя</label>
                <input type="text" id="nickname" name="nickname" required>
                <div class="helper" id="nicknameHint">От 4 до скольки-то там символов</div>
            </div>
            <div>
                <label for="password">Пароль</label>
                <input type="password" id="password" name="password" required>
                <div class="helper" id="passwordHint">От 4 до скольки-то там символов</div>
            </div>
            <div>
                <label for="confirm">Повторите пароль</label>
                <input type="password" id="confirm" name="confirm" required>
            </div>
        </div>
        <div class="actions">
            <button class="log-button" type="submit">Зарегистрироваться</button>
            <a class="reg-text" href="login.jsp">Войти</a>
        </div>
    </form>
</div>
<script src="js/theme.js"></script>
</body>
</html>
