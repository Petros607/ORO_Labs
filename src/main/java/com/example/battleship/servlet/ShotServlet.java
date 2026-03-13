package com.example.battleship.servlet;

import com.example.battleship.model.GameSession;
import com.example.battleship.model.ShotResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Обработка выстрела по координатам.
 */
@WebServlet(name = "ShotServlet", urlPatterns = {"/shot"})
public class ShotServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("game") == null) {
            req.setAttribute("error", "Игровая сессия не найдена. Начните новую игру.");
            req.getRequestDispatcher("index.jsp").forward(req, resp);
            return;
        }

        GameSession game = (GameSession) httpSession.getAttribute("game");

        String rowParam = req.getParameter("row");
        String colParam = req.getParameter("col");
        ShotResult result;

        try {
            int row = Integer.parseInt(rowParam) - 1; // пользователь вводит с 1
            int col = Integer.parseInt(colParam) - 1;
            result = game.shoot(row, col);
        } catch (NumberFormatException e) {
            result = ShotResult.invalid("Координаты должны быть целыми числами");
        }

        req.setAttribute("shotResult", result);
        // Сессия уже обновлена внутри GameSession
        req.getRequestDispatcher("game.jsp").forward(req, resp);
    }
}


