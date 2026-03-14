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

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String nickname = req.getParameter("nickname");
        String password = req.getParameter("password");

        if (nickname == null || password == null || nickname.trim().isEmpty() || password.trim().isEmpty()) {
            req.setAttribute("error", "Введите ник и пароль");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = DBUtils.authenticate(nickname, password);
            if (user == null) {
                req.setAttribute("error", "Неверный логин или пароль");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            log.log(Level.INFO, "User logged in: {0}", user.getNickname());
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Login error", e);
            req.setAttribute("error", "Ошибка при входе");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }
}
