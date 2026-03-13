<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Победа!</title>
</head>
<body>
    <h1>Ура! Компьютер угадал число: <%= session.getAttribute("guess") %></h1>
    <p>Хотите сыграть еще раз?</p>
    <a href="index.jsp">Начать новую игру</a>
</body>
</html>