<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="com.example.battleship.model.GameSession" %>
<%@ page import="com.example.battleship.model.CellState" %>
<%@ page import="com.example.battleship.model.GameStatus" %>
<%@ page import="com.example.battleship.model.ShotResult" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Морской бой – игра</title>
    <style>
        :root {
            --bg-main: radial-gradient(circle at top, #1a365d, #0f172a);
            --bg-panel: rgba(15, 23, 42, 0.96);
            --border-panel: rgba(148, 163, 184, 0.35);
            --text-main: #e5e7eb;
            --text-muted: #9ca3af;
            --board-bg: #020617;
            --board-header-bg: #020617;
            --cell-border: #1f2937;
            --cell-available: #0369a1;   /* доступные клетки — ярко-синие */
            --cell-inactive: #020617;    /* отстрелянные/соседние — почти чёрные */
            --stats-bg: rgba(15,23,42,0.9);
            --stats-border: rgba(148,163,184,0.4);
            --btn-secondary-bg: rgba(15,23,42,1);
            --btn-secondary-text: #e5e7eb;
            --btn-secondary-border: rgba(148,163,184,0.7);
            --modal-bg: #020617;
        }
        .theme-light {
            --bg-main: radial-gradient(circle at top, #e5f0ff, #e5e7eb);
            --bg-panel: rgba(255, 255, 255, 0.98);
            --border-panel: rgba(148, 163, 184, 0.55);
            --text-main: #0f172a;
            --text-muted: #6b7280;
            --board-bg: #f9fafb;
            --board-header-bg: #e5e7eb;
            --cell-border: #d1d5db;
            --cell-available: #bae6fd;   /* доступные клетки — заметный голубой */
            --cell-inactive: #f3f4f6;    /* отстрелянные/соседние — светло-серые */
            --stats-bg: #f3f4f6;
            --stats-border: #d1d5db;
            --btn-secondary-bg: #ffffff;
            --btn-secondary-text: #0f172a;
            --btn-secondary-border: #cbd5e1;
            --modal-bg: #ffffff;
        }
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            background: var(--bg-main);
            color: var(--text-main);
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .layout {
            display: grid;
            grid-template-columns: auto 260px;
            gap: 32px;
            padding: 32px 40px;
            border-radius: 18px;
            background: var(--bg-panel);
            box-shadow: 0 30px 60px rgba(15,23,42,0.9);
            border: 1px solid var(--border-panel);
        }
        h1 {
            margin: 0 0 8px;
            font-size: 26px;
            letter-spacing: 0.08em;
            text-transform: uppercase;
        }
        .subtitle {
            margin-bottom: 18px;
            color: var(--text-muted);
            font-size: 13px;
        }
        table.board {
            border-collapse: collapse;
            background: var(--board-bg);
            border-radius: 14px;
            overflow: hidden;
        }
        table.board th,
        table.board td {
            width: 28px;
            height: 28px;
            text-align: center;
            font-size: 13px;
        }
        table.board th {
            background: var(--board-header-bg);
            color: var(--text-muted);
            font-weight: 500;
        }
        table.board td {
            border: 1px solid var(--cell-border);
            cursor: pointer;
            transition: background 0.08s ease-out, transform 0.05s ease-out;
        }
        table.board td:hover {
            transform: translateY(-1px);
            background: rgba(15,23,42,0.9);
        }
        .cell-hit {
            background: radial-gradient(circle, #f97316, #b91c1c 70%);
            color: #fef3c7;
            font-weight: 700;
        }
        .cell-miss {
            background: var(--cell-inactive);
            color: #6b7280;
        }
        .cell-near {
            background: var(--cell-inactive);
            color: #4b5563;
            font-size: 11px;
        }
        .cell-empty {
            background: var(--cell-available);
        }
        .panel {
            display: flex;
            flex-direction: column;
            gap: 16px;
            min-width: 0;
        }
        .stats {
            padding: 12px 14px;
            border-radius: 12px;
            background: var(--stats-bg);
            border: 1px solid var(--stats-border);
            font-size: 13px;
        }
        .stats-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
        }
        .badge {
            display: inline-flex;
            align-items: center;
            padding: 3px 10px;
            border-radius: 999px;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }
        .badge-live {
            background: rgba(34,197,94,0.1);
            color: #bbf7d0;
            border: 1px solid rgba(34,197,94,0.6);
        }
        .badge-won {
            background: rgba(59,130,246,0.15);
            color: #bfdbfe;
            border: 1px solid rgba(59,130,246,0.7);
        }
        .badge-lost {
            background: rgba(239,68,68,0.15);
            color: #fecaca;
            border: 1px solid rgba(248,113,113,0.7);
        }
        .message {
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 13px;
        }
        .message-info {
            background: rgba(15,118,110,0.2);
            border: 1px solid rgba(45,212,191,0.6);
            color: #a5f3fc;
        }
        .message-error {
            background: rgba(239,68,68,0.15);
            border: 1px solid rgba(248,113,113,0.7);
            color: #fecaca;
        }
        form.shoot {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 10px 12px;
            font-size: 13px;
        }
        label {
            display: block;
            margin-bottom: 4px;
        }
        input[type="number"] {
            width: 100%;
            padding: 7px 9px;
            border-radius: 8px;
            border: 1px solid #4b5563;
            background: #020617;
            color: #e5e7eb;
            font-size: 13px;
        }
        input[type="number"]:focus {
            outline: none;
            border-color: #38bdf8;
            box-shadow: 0 0 0 1px #38bdf8;
        }
        .btn-primary,
        .btn-secondary {
            border: none;
            border-radius: 999px;
            padding: 8px 18px;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            cursor: pointer;
            transition: transform 0.08s ease-out, box-shadow 0.08s ease-out, filter 0.05s ease-out;
            white-space: nowrap;
        }
        .btn-primary {
            background: linear-gradient(135deg, #0ea5e9, #22c55e);
            color: #020617;
            box-shadow: 0 14px 30px rgba(34,197,94,0.35);
        }
        .btn-secondary {
            background: var(--btn-secondary-bg);
            color: var(--btn-secondary-text);
            border: 1px solid var(--btn-secondary-border);
        }
        .btn-primary:hover,
        .btn-secondary:hover {
            transform: translateY(-1px);
            filter: brightness(1.05);
        }
        .btn-primary:active,
        .btn-secondary:active {
            transform: translateY(0);
        }
        .actions {
            display: flex;
            gap: 8px;
            margin-top: 6px;
            justify-content: flex-end;
        }
        .overlay {
            position: fixed;
            inset: 0;
            background: rgba(15,23,42,0.85);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 40;
        }
        .modal {
            background: var(--modal-bg);
            border-radius: 18px;
            padding: 24px 26px;
            max-width: 380px;
            width: calc(100% - 32px);
            box-shadow: 0 24px 60px rgba(15,23,42,0.95);
            border: 1px solid rgba(148,163,184,0.6);
        }
        .modal-title {
            font-size: 20px;
            font-weight: 700;
            margin-bottom: 6px;
        }
        .modal-text {
            font-size: 14px;
            color: #9ca3af;
            margin-bottom: 16px;
            word-wrap: break-word;
            word-break: break-word;
        }
        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        .top-bar {
            position: absolute;
            top: 12px;
            right: 16px;
            display: flex;
            gap: 8px;
            font-size: 11px;
        }
        .pill-button {
            border-radius: 999px;
            border: 1px solid rgba(148,163,184,0.7);
            padding: 4px 10px;
            background: transparent;
            color: var(--text-muted);
            cursor: pointer;
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }
    </style>
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
        <button type="button" class="pill-button" id="themeToggle">Тёмная тема</button>
    </div>
    <div>
        <h1>Морской бой</h1>
        <div class="subtitle">Одинарные цели, не касаются друг друга ни сторонами, ни углами.</div>

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
                    if (cell == CellState.MISS) {
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

        function applyTheme(theme) {
            document.body.classList.remove('theme-light');
            if (theme === 'light') {
                document.body.classList.add('theme-light');
                themeToggle.textContent = 'Светлая тема';
            } else {
                themeToggle.textContent = 'Тёмная тема';
            }
        }

        const savedTheme = window.localStorage.getItem('theme') || 'dark';
        applyTheme(savedTheme);

        themeToggle.addEventListener('click', function () {
            const current = document.body.classList.contains('theme-light') ? 'light' : 'dark';
            const next = current === 'light' ? 'dark' : 'light';
            window.localStorage.setItem('theme', next);
            applyTheme(next);
        });

        function startMusicOnce() {
            if (!bgMusic) return;
            bgMusic.volume = 0.25;
            bgMusic.play().catch(function () {});
            document.removeEventListener('click', startMusicOnce);
        }
        document.addEventListener('click', startMusicOnce);

        if (!finished) {
            cells.forEach(function (cell) {
                cell.addEventListener('click', function () {
                    if (cell.classList.contains('cell-hit')
                        || cell.classList.contains('cell-miss')
                        || cell.classList.contains('cell-near')) {
                        return;
                    }
                    const row = cell.getAttribute('data-row');
                    const col = cell.getAttribute('data-col');
                    rowField.value = row;
                    colField.value = col;
                    form.submit();
                });
            });
        }

        function playDeferredSounds() {
            if (lastResultValid) {
                if (lastResultHit && hitSound) {
                    hitSound.play().catch(function () {});
                } else if (shotSound) {
                    shotSound.play().catch(function () {});
                }
            }

            if (finished) {
                if (statusNow === 'WON' && winSound) {
                    winSound.play().catch(function () {});
                } else if (statusNow === 'LOST' && loseSound) {
                    loseSound.play().catch(function () {});
                }
            }
        }

        // Чтобы не упираться в политику автопроигрывания,
        // звуки попадания/победы/поражения играем при первом клике после перезагрузки страницы.
        if (lastResultValid || finished) {
            function onFirstClick() {
                playDeferredSounds();
                document.removeEventListener('click', onFirstClick);
            }
            document.addEventListener('click', onFirstClick);
        }
    })();
</script>
</body>
</html>