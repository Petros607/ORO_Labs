<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Морской бой – новая игра</title>
    <style>
        :root {
            --bg-main: radial-gradient(circle at top, #1a365d, #0f172a);
            --bg-card: rgba(15, 23, 42, 0.95);
            --border-card: rgba(148, 163, 184, 0.35);
            --text-main: #e5e7eb;
            --text-muted: #9ca3af;
            --input-bg: #020617;
            --input-border: #4b5563;
        }
        .theme-light {
            --bg-main: radial-gradient(circle at top, #e5f0ff, #e5e7eb);
            --bg-card: rgba(255, 255, 255, 0.96);
            --border-card: rgba(148, 163, 184, 0.55);
            --text-main: #0f172a;
            --text-muted: #6b7280;
            --input-bg: #f9fafb;
            --input-border: #cbd5f5;
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
        .card {
            background: var(--bg-card);
            border-radius: 16px;
            box-shadow: 0 24px 60px rgba(15,23,42,0.8);
            padding: 32px 40px;
            max-width: 520px;
            width: 100%;
            border: 1px solid var(--border-card);
            position: relative;
            overflow: hidden;
        }
        h1 {
            margin: 0 0 8px;
            font-size: 28px;
            letter-spacing: 0.04em;
            text-transform: uppercase;
        }
        .subtitle {
            margin-bottom: 24px;
            color: var(--text-muted);
            font-size: 14px;
        }
        label {
            display: block;
            font-size: 13px;
            margin-bottom: 4px;
            color: var(--text-main);
        }
        input {
            width: 100%;
            padding: 8px 10px;
            border-radius: 8px;
            border: 1px solid var(--input-border);
            background: var(--input-bg);
            color: var(--text-main);
            font-size: 14px;
        }
        input:focus {
            outline: none;
            border-color: #38bdf8;
            box-shadow: 0 0 0 1px #38bdf8;
        }
        .grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px 20px;
            margin-bottom: 20px;
        }
        .helper {
            font-size: 12px;
            color: var(--text-muted);
        }
        .error {
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(248, 113, 113, 0.7);
            color: #fecaca;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 13px;
            margin-bottom: 18px;
        }
        .actions {
            display: flex;
            justify-content: flex-end;
            margin-top: 8px;
        }
        button {
            border: none;
            border-radius: 999px;
            padding: 10px 22px;
            font-size: 14px;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            cursor: pointer;
            background: linear-gradient(135deg, #0ea5e9, #22c55e);
            color: #0b1120;
            box-shadow: 0 14px 30px rgba(34,197,94,0.35);
            transition: transform 0.08s ease-out, box-shadow 0.08s ease-out, filter 0.05s ease-out;
        }
        button:hover {
            transform: translateY(-1px);
            filter: brightness(1.05);
            box-shadow: 0 18px 40px rgba(34,197,94,0.5);
        }
        button:active {
            transform: translateY(0);
            box-shadow: 0 10px 24px rgba(34,197,94,0.4);
        }
        .top-bar {
            position: absolute;
            top: 10px;
            right: 10px;
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
        .toast {
            position: absolute;
            left: 24px;
            right: 24px;
            bottom: 20px;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 13px;
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(248, 113, 113, 0.7);
            color: #fecaca;
            display: none;
        }
    </style>
</head>
<body>
<div class="card">
    <div class="top-bar">
        <button type="button" class="pill-button" id="themeToggle">Тёмная тема</button>
    </div>
    <h1>Морской бой</h1>
    <div class="subtitle">Настройте параметры поля и количество кораблей. Все цели занимают одну клетку и не касаются друг друга.</div>

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

        // Запускаем фоновую музыку по первому взаимодействию
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
</html>
