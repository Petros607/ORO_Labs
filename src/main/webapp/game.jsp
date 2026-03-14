<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="com.example.battleship.model.GameSession" %>
<%@ page import="com.example.battleship.model.CellState" %>
<%@ page import="com.example.battleship.model.GameStatus" %>
<%@ page import="com.example.battleship.model.ShotResult" %>
<%@ page import="com.example.battleship.model.User" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Морской бой – игра</title>
    <link rel="stylesheet" href="css/game.css">
</head>
<body>
<%
    GameSession game = (GameSession) session.getAttribute("game");
    if (game == null) {
%>
    <script>window.location.href = "index.jsp";</script>
<%
        return;
    }
    ShotResult shotResult = (ShotResult) request.getAttribute("shotResult");
    CellState[][] field = game.getField();
    GameStatus status = game.getStatus();
    boolean finished = game.isFinished();
%>
<div class="layout">
    <div class="top-bar">
        <button type="button" class="theme-button" id="themeToggle">Тёмная тема</button>
        <div class="auth-chip">
            <%
                User currentUser = (User) session.getAttribute("user");
                if (currentUser == null) {
            %>
            <a class="log-button" href="login.jsp" style="min-width: 80px;">Войти</a>
            <% } else { %>
            <span class="nickname"><%= currentUser.getNickname() %></span>
            <a class="log-button" href="logout">Выйти</a>
            <% } %>
        </div>
    </div>
    <div class="game-class">
        <h1>Морской бой</h1>
        <div class="subtitle">Однопалубные корабли не касаются друг друга.</div>

        <form id="shootForm" method="post" action="shot">
            <input type="hidden" name="row" id="rowField">
            <input type="hidden" name="col" id="colField">
        <table class="board">
            <tr>
                <th></th>
                <% for (int c = 0; c < game.getCols(); c++) { %>
                <th><%= (c + 1) %></th>
                <% } %>
            </tr>
            <% for (int r = 0; r < game.getRows(); r++) { %>
            <tr>
                <th><%= (r + 1) %></th>
                <% for (int c = 0; c < game.getCols(); c++) {
                    CellState cell = field[r][c];
                    String cls = "cell-empty";
                    String symbol = "";
                    if (finished && status == GameStatus.LOST && cell == CellState.TARGET) {
                        cls = "cell-target-revealed";
                        symbol = "●";
                    } else if (cell == CellState.MISS) {
                        cls = "cell-miss";
                        symbol = "•";
                    } else if (cell == CellState.HIT) {
                        cls = "cell-hit";
                        symbol = "X";
                    } else if (cell == CellState.NEAR) {
                        cls = "cell-near";
                        symbol = "·";
                    }
                %>
                <td class="<%= cls %>"
                    data-row="<%= (r + 1) %>"
                    data-col="<%= (c + 1) %>"><%= symbol %></td>
                <% } %>
            </tr>
            <% } %>
        </table>
        </form>
    </div>

    <div class="panel">
        <div class="stats">
            <div class="stats-row">
                <span>Статус</span>
                <span>
                <%
                    if (status == GameStatus.IN_PROGRESS) {
                %>
                    <span class="badge badge-live">В процессе</span>
                <% } else if (status == GameStatus.WON) { %>
                    <span class="badge badge-won">Победа</span>
                <% } else { %>
                    <span class="badge badge-lost">Поражение</span>
                <% } %>
                </span>
            </div>
            <div class="stats-row">
                <span>Целей уничтожено</span>
                <span><%= game.getDestroyedTargets() %> / <%= game.getTotalTargets() %></span>
            </div>
            <div class="stats-row">
                <span>Оставшиеся выстрелы</span>
                <span><%= game.getRemainingShots() %></span>
            </div>
        </div>

        <% if (shotResult != null) { %>
        <div class="message <%= shotResult.isValid() ? "message-info" : "message-error" %>">
            <%= shotResult.getMessage() %>
        </div>
        <% } %>

        <div class="actions">
            <button type="button" class="btn-secondary"
                    onclick="window.location.href='index.jsp'">Новая игра</button>
        </div>
    </div>
</div>
<% if (finished) { %>
<div class="overlay">
    <div class="modal">
        <div class="modal-title">
            <%= status == GameStatus.WON ? "Победа!" : "Поражение" %>
        </div>
        <div class="modal-text">
            <% if (status == GameStatus.WON) { %>
                Вы уничтожили все цели за <%= game.getTotalTargets() %> попаданий. Отличная работа, командир!
            <% } else { %>
                Выстрелы закончились, а цели ещё остались на поле. Попробуйте изменить параметры или стратегию.
            <% } %>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn-secondary"
                    onclick="document.querySelector('.overlay').style.display='none'">
                Продолжить просмотр поля
            </button>
            <button type="button" class="btn-primary"
                    onclick="window.location.href='index.jsp'">
                Новая игра
            </button>
        </div>
    </div>
</div>
<% } %>
<audio id="bgMusic" src="audio/bg-music.mp3" loop></audio>
<audio id="shotSound" src="audio/shot.mp3"></audio>
<audio id="hitSound" src="audio/hit.mp3"></audio>
<audio id="winSound" src="audio/win.mp3"></audio>
<audio id="loseSound" src="audio/lose.mp3"></audio>
<script>
    (function () {
        const finished = <%= finished ? "true" : "false" %>;
        const lastResultValid = <%= (shotResult != null && shotResult.isValid()) ? "true" : "false" %>;
        const lastResultHit = <%= (shotResult != null && shotResult.getHit() != null && shotResult.getHit()) ? "true" : "false" %>;
        const statusNow = "<%= status %>";

        const form = document.getElementById('shootForm');
        const rowField = document.getElementById('rowField');
        const colField = document.getElementById('colField');
        const cells = document.querySelectorAll('table.board td');
        const themeToggle = document.getElementById('themeToggle');

        const bgMusic = document.getElementById('bgMusic');
        const shotSound = document.getElementById('shotSound');
        const hitSound = document.getElementById('hitSound');
        const winSound = document.getElementById('winSound');
        const loseSound = document.getElementById('loseSound');

        function startMusicOnce() {
            if (!bgMusic) return;
            bgMusic.volume = 0.25;
            bgMusic.play().catch(function () {});

            // При первом разрешённом клике пробуем также проиграть звук
            // попадания/победы/поражения поверх фоновой музыки.
            if (lastResultValid && lastResultHit && hitSound) {
                hitSound.play().catch(function () {});
            }
            if (finished) {
                if (statusNow === 'WON' && winSound) {
                    winSound.play().catch(function () {});
                } else if (statusNow === 'LOST' && loseSound) {
                    loseSound.play().catch(function () {});
                }
            }

            document.removeEventListener('click', startMusicOnce);
        }
        document.addEventListener('click', startMusicOnce);

        if (!finished) {
            cells.forEach(function (cell) {
                cell.addEventListener('click', function () {
                    if (cell.classList.contains('cell-hit')
                        || cell.classList.contains('cell-miss')
                        || cell.classList.contains('cell-near')
                        || cell.classList.contains('cell-target-revealed')) {
                        return;
                    }
                    const row = cell.getAttribute('data-row');
                    const col = cell.getAttribute('data-col');
                    rowField.value = row;
                    colField.value = col;

                    // Звук выстрела всегда в момент клика по клетке
                    if (shotSound) {
                        shotSound.currentTime = 0;
                        shotSound.play().catch(function () {});
                    }

                    form.submit();
                });
            });
        }
    })();
</script>
<script src="js/theme.js"></script>
</body>
</html>