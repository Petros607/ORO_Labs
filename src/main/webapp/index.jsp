<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="com.example.battleship.model.User" %>
<%@ page import="com.example.battleship.model.DBUtils" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Морской бой – новая игра</title>
    <link rel="stylesheet" href="css/index.css">
</head>
<body>
<div class="card">
    <div class="top-bar">
        <button type="button" class="pill-button" id="themeToggle">Тёмная тема</button>
        <div class="auth-chip">
            <%
                User currentUser = (User) session.getAttribute("user");
                if (currentUser == null) {
            %>
                <a class="pill-button" href="login.jsp" style="min-width: 80px;">Войти</a>
            <% } else { %>
                <span style="color: #d1d5db; font-size: 11px; margin-right: 8px;">Привет, <strong><%= currentUser.getNickname() %></strong></span>
                <a class="pill-button" href="logout">Выйти</a>
            <% } %>
        </div>
    </div>
    <h1>Морской бой</h1>
    <div class="subtitle">Настройте параметры поля и количество кораблей. Все цели занимают одну клетку и не касаются друг друга.</div>

    <%
        List<User> leaderboard = DBUtils.getTopPlayers(5);
        if (leaderboard != null && !leaderboard.isEmpty()) {
    %>
    <div class="leaderboard">
        <h3>Топ-5 игроков</h3>
        <table>
            <thead>
            <tr><th>#</th><th>Ник</th><th>Побед</th></tr>
            </thead>
            <tbody>
            <% int pos = 1; for (User u : leaderboard) { %>
                <tr>
                    <td><%= pos++ %></td>
                    <td><%= u.getNickname() %></td>
                    <td><%= u.getCountOfWins() %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="error">
        <strong>Ошибка:</strong>
        <%= (error == null || error.trim().isEmpty()) ? "Проверьте корректность введённых параметров." : error %>
    </div>
    <% } %>

    <form method="post" action="new-game">
        <div class="grid">
            <div>
                <label for="rows">Количество строк (10–20)</label>
                <input type="number" id="rows" name="rows" min="10" max="20" value="10" required>
            </div>
            <div>
                <label for="cols">Количество столбцов (10–20)</label>
                <input type="number" id="cols" name="cols" min="10" max="20" value="10" required>
            </div>
            <div>
                <label for="targets">Количество целей</label>
                <input type="number" id="targets" name="targets" min="1" value="10" required>
                <div class="helper" id="targetsHint">1 ≤ цели ≤ ⌈N/2⌉ × ⌈M/2⌉</div>
            </div>
            <div>
                <label for="shots">Количество выстрелов</label>
                <input type="number" id="shots" name="shots" min="1" value="30" required>
                <div class="helper" id="shotsHint">цели ≤ выстрелы ≤ N × M</div>
            </div>
        </div>
        <div class="actions">
            <button type="submit">Начать игру</button>
        </div>
    </form>
</div>
<audio id="bgMusic" src="audio/bg-music.mp3" loop></audio>
<div class="toast" id="inputToast"></div>
<script>
    (function () {
        const rowsInput = document.getElementById('rows');
        const colsInput = document.getElementById('cols');
        const targetsInput = document.getElementById('targets');
        const shotsInput = document.getElementById('shots');
        const targetsHint = document.getElementById('targetsHint');
        const shotsHint = document.getElementById('shotsHint');
        const themeToggle = document.getElementById('themeToggle');
        const toast = document.getElementById('inputToast');
        const bgMusic = document.getElementById('bgMusic');

        function ceilHalf(x) {
            return Math.ceil(x / 2);
        }

        function updateHints() {
            const n = parseInt(rowsInput.value, 10) || 0;
            const m = parseInt(colsInput.value, 10) || 0;

            const maxTargets = ceilHalf(n) * ceilHalf(m);
            const minTargets = 1;
            targetsInput.min = String(minTargets);
            targetsInput.max = String(maxTargets);
            targetsHint.textContent = minTargets + " <= цели <= " + maxTargets;

            const currentTargets = parseInt(targetsInput.value || minTargets, 10);
            const minShots = Math.max(minTargets, currentTargets);
            const maxShots = n * m || 0;
            shotsInput.min = String(minShots);
            shotsInput.max = String(maxShots);
            shotsHint.textContent = minShots + " <= выстрелы <= " + maxShots;
        }

        function showToast(message) {
            toast.textContent = message;
            toast.style.display = 'block';
            setTimeout(function () {
                toast.style.display = 'none';
            }, 2000);
        }

        function validateRange(input, min, max, fieldName) {
            const value = parseInt(input.value, 10);
            if (isNaN(value)) return;
            if (value < min || value > max) {
                showToast(fieldName + " должно быть в диапазоне " + min + "–" + max);
            }
        }

        rowsInput.addEventListener('change', function () {
            validateRange(rowsInput, 10, 20, "Количество строк");
            updateHints();
        });
        colsInput.addEventListener('change', function () {
            validateRange(colsInput, 10, 20, "Количество столбцов");
            updateHints();
        });
        targetsInput.addEventListener('change', function () {
            const maxTargets = parseInt(targetsInput.max || "1", 10);
            validateRange(targetsInput, 1, maxTargets, "Количество целей");
            updateHints();
        });

        rowsInput.addEventListener('input', updateHints);
        colsInput.addEventListener('input', updateHints);
        targetsInput.addEventListener('input', updateHints);
        shotsInput.addEventListener('change', function () {
            const minShots = parseInt(shotsInput.min || "1", 10);
            const maxShots = parseInt(shotsInput.max || "100", 10);
            validateRange(shotsInput, minShots, maxShots, "Количество выстрелов");
        });

        // Запускаем фоновую музыку по первому взаимодействия
        function startMusicOnce() {
            if (!bgMusic) return;
            bgMusic.volume = 0.25;
            bgMusic.play().catch(function () {});
            document.removeEventListener('click', startMusicOnce);
        }
        document.addEventListener('click', startMusicOnce);

        updateHints();
    })();
</script>
<script src="js/theme.js"></script>
</body>
</html>
