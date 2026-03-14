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

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());

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

        if (nickname == null || password == null || confirm == null || nickname.trim().isEmpty() || password.trim().isEmpty() || confirm.trim().isEmpty()) {
            req.setAttribute("error", "Заполните все поля");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirm)) {
            req.setAttribute("error", "Пароли не совпадают");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (password.length() < 4) {
            req.setAttribute("error", "Пароль должен быть не менее 4 символов");
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
