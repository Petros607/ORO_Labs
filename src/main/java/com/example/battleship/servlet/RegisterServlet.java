package com.example.battleship.servlet;

import com.example.battleship.model.DBUtils;
import com.example.battleship.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());
    // Регулярное выражение для проверки логина (только буквы, цифры и нижнее подчеркивание)
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    // Регулярное выражение для проверки пароля (минимум: одна буква, одна цифра)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-zA-Z0-9!@#$%^&*]+$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String nickname = req.getParameter("nickname");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");

        // Проверка на пустые поля
        if (nickname == null || password == null || confirm == null ||
                nickname.trim().isEmpty() || password.trim().isEmpty() || confirm.trim().isEmpty()) {
            req.setAttribute("error", "Заполните все поля");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        nickname = nickname.trim();
        password = password.trim();
        confirm = confirm.trim();

        // Проверка длины логина (3-15 символов)
        if (nickname.length() < 3) {
            req.setAttribute("error", "Логин должен содержать не менее 3 символов");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (nickname.length() > 15) {
            req.setAttribute("error", "Логин не должен превышать 15 символов");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // Проверка логина на наличие только разрешенных символов
        if (!LOGIN_PATTERN.matcher(nickname).matches()) {
            req.setAttribute("error", "Логин может содержать только латинские буквы, цифры и символ подчеркивания");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // Проверка длины пароля (6-15 символов)
        if (password.length() < 6) {
            req.setAttribute("error", "Пароль должен содержать не менее 6 символов");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (password.length() > 15) {
            req.setAttribute("error", "Пароль не должен превышать 15 символов");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // Проверка пароля на наличие запрещенных символов
        if (!ALLOWED_PATTERN.matcher(password).matches()) {
            req.setAttribute("error", "Пароль содержит недопустимые символы. Разрешены только латинские буквы, цифры и символы !@#$%^&*");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // Проверка сложности пароля (должен содержать хотя бы одну букву и одну цифру)
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            req.setAttribute("error", "Пароль должен содержать хотя бы одну букву и одну цифру");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirm)) {
            req.setAttribute("error", "Пароли не совпадают");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        try {
            User user = DBUtils.register(nickname, password);
            if (user == null) {
                req.setAttribute("error", "Пользователь уже существует или ошибка регистрации");
                req.getRequestDispatcher("register.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            log.log(Level.INFO, "User registered: {0}", user.getNickname());
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Register error", e);
            req.setAttribute("error", "Ошибка при регистрации");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
        }
    }
}
