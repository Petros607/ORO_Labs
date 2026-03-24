<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="error.jsp" %>
<%
    String action = request.getParameter("action");

    if ("start".equals(action)) {
        try {
            int min = Integer.parseInt(request.getParameter("min"));
            int max = Integer.parseInt(request.getParameter("max"));

            if (min >= max) {
                throw new IllegalArgumentException("Минимальное значение должно быть меньше максимального");
            }

            HttpSession gameSession = request.getSession();
            gameSession.setAttribute("min", min);
            gameSession.setAttribute("max", max);
            int guess = (min + max) / 2;
            gameSession.setAttribute("guess", guess);

            request.getRequestDispatcher("/game.jsp").forward(request, response);
            return;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Пожалуйста, введите корректные числа");
        }
    }
%>
<html>
<head>
    <title>Угадай число</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Добро пожаловать в игру "Угадай число"!</h1>
        <p>Компьютер будет пытаться угадать число, которое вы загадаете.</p>
        <form action="index.jsp" method="post">
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
