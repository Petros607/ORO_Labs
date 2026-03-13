<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Игра</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Компьютер предполагает: <%= session.getAttribute("guess") %></h1>
        <p>Ваше число больше, меньше или равно этому?</p>
        <form action="game" method="get">
            <input type="hidden" name="action" value="guess">
            <button type="submit" name="response" value="higher">Больше</button>
            <button type="submit" name="response" value="equal">Равно</button>
            <button type="submit" name="response" value="lower">Меньше</button>
        </form>
    </div>
</body>
</html>