<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Игра</title>
</head>
<body>
    <h1>Компьютер предполагает: <%= session.getAttribute("guess") %></h1>
    <p>Ваше число больше, меньше или равно этому?</p>
    <form action="game" method="get">
        <input type="hidden" name="action" value="guess">
        <input type="submit" name="response" value="higher" label="Больше">
        <input type="submit" name="response" value="equal" label="Равно">
        <input type="submit" name="response" value="lower" label="Меньше">
    </form>
</body>
</html>