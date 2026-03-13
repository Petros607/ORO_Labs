<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Морской бой – новая игра</title>
    <style>
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            background: radial-gradient(circle at top, #1a365d, #0f172a);
            color: #e5e7eb;
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .card {
            background: rgba(15, 23, 42, 0.95);
            border-radius: 16px;
            box-shadow: 0 24px 60px rgba(15,23,42,0.8);
            padding: 32px 40px;
            max-width: 520px;
            width: 100%;
            border: 1px solid rgba(148, 163, 184, 0.35);
        }
        h1 {
            margin: 0 0 8px;
            font-size: 28px;
            letter-spacing: 0.04em;
            text-transform: uppercase;
        }
        .subtitle {
            margin-bottom: 24px;
            color: #9ca3af;
            font-size: 14px;
        }
        label {
            display: block;
            font-size: 13px;
            margin-bottom: 4px;
            color: #e5e7eb;
        }
        input {
            width: 100%;
            padding: 8px 10px;
            border-radius: 8px;
            border: 1px solid #4b5563;
            background: #020617;
            color: #e5e7eb;
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
            color: #9ca3af;
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
    </style>
</head>
<body>
<div class="card">
    <h1>Морской бой</h1>
    <div class="subtitle">Настройте параметры поля и количество кораблей. Все цели занимают одну клетку и не касаются друг друга.</div>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="error"><%= error %></div>
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
</body>
<script>
    (function () {
        const rowsInput = document.getElementById('rows');
        const colsInput = document.getElementById('cols');
        const targetsInput = document.getElementById('targets');
        const shotsInput = document.getElementById('shots');
        const targetsHint = document.getElementById('targetsHint');
        const shotsHint = document.getElementById('shotsHint');

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

            const minShots = Math.max(minTargets, parseInt(targetsInput.value || minTargets, 10));
            const maxShots = n * m || 0;
            shotsInput.min = String(minShots);
            shotsInput.max = String(maxShots);
            shotsHint.textContent = "цели <= выстрелы <= " + (maxShots || "N × M");
        }

        rowsInput.addEventListener('input', updateHints);
        colsInput.addEventListener('input', updateHints);
        targetsInput.addEventListener('input', updateHints);

        updateHints();
    })();
</script>
</html>
