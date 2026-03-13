<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Угадай число</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Добро пожаловать в игру "Угадай число"!</h1>
        <p>Компьютер будет пытаться угадать число, которое вы загадаете.</p>
        <form action="game" method="get">
            <input type="hidden" name="action" value="start">
            <label for="min">Минимальное значение:</label>
            <input type="number" id="min" name="min" required><br><br>
            <label for="max">Максимальное значение:</label>
            <input type="number" id="max" name="max" required><br><br>
            <input type="submit" value="Начать игру">
        </form>
    </div>
</body>
</html>