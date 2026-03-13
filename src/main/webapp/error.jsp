<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<html>
<head>
    <title>Ошибка</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Произошла ошибка!</h1>
        <p><%= exception.getMessage() %></p>
        <a href="javascript:history.back()">Вернуться назад</a>
    </div>
</body>
</html>