<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page errorPage="error.jsp" %>
<%
    HttpSession gameSession = request.getSession();
    String action = request.getParameter("action");

    // Проверяем, есть ли игра в сессии
    if (gameSession.getAttribute("min") == null && !"start".equals(action)) {
        response.sendRedirect("index.jsp");
        return;
    }

    if ("guess".equals(action)) {
        String responseParam = request.getParameter("response");
        int min = (Integer) gameSession.getAttribute("min");
        int max = (Integer) gameSession.getAttribute("max");
        int guess = (Integer) gameSession.getAttribute("guess");

        if ("higher".equals(responseParam)) {
            min = guess + 1;
        } else if ("lower".equals(responseParam)) {
            max = guess - 1;
        } else if ("equal".equals(responseParam)) {
            request.getRequestDispatcher("/win.jsp").forward(request, response);
            return;
        }

        if (min > max) {
            request.getRequestDispatcher("/cheat.jsp").forward(request, response);
            return;
        }

        guess = (min + max) / 2;
        gameSession.setAttribute("min", min);
        gameSession.setAttribute("max", max);
        gameSession.setAttribute("guess", guess);
    }
%>
<html>
<head>
    <title>Игра</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>Компьютер предполагает: <%= gameSession.getAttribute("guess") %></h1>
        <p>Ваше число больше, меньше или равно этому?</p>
        <form action="game.jsp" method="post">
            <input type="hidden" name="action" value="guess">
            <button type="submit" name="response" value="higher">Больше</button>
            <button type="submit" name="response" value="equal">Равно</button>
            <button type="submit" name="response" value="lower">Меньше</button>
        </form>
    </div>
</body>
</html>
