(function () {
    const themeToggle = document.getElementById('themeToggle');
    if (!themeToggle) {
        return;
    }

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
})();
