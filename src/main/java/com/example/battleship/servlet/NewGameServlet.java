package com.example.battleship.servlet;

import com.example.battleship.model.GameSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Инициализация новой игры.
 */
@WebServlet(name = "NewGameServlet", urlPatterns = {"/new-game"})
public class NewGameServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(NewGameServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String rowsParam = req.getParameter("rows");
        String colsParam = req.getParameter("cols");
        String targetsParam = req.getParameter("targets");
        String shotsParam = req.getParameter("shots");

        try {
            int rows = Integer.parseInt(rowsParam);
            int cols = Integer.parseInt(colsParam);
            int targets = Integer.parseInt(targetsParam);
            int shots = Integer.parseInt(shotsParam);

            String error = validateParams(rows, cols, targets, shots);
            if (error != null) {
                log.log(Level.WARNING, "Invalid new game parameters: rows={0}, cols={1}, targets={2}, shots={3}, reason={4}",
                        new Object[]{rows, cols, targets, shots, error});
                req.setAttribute("error", error);
                req.getRequestDispatcher("index.jsp").forward(req, resp);
                return;
            }

            GameSession session = new GameSession(rows, cols, targets, shots);
            HttpSession httpSession = req.getSession(true);
            httpSession.setAttribute("game", session);

            log.log(Level.INFO, "New game created: sessionId={0}, rows={1}, cols={2}, targets={3}, shots={4}",
                    new Object[]{httpSession.getId(), rows, cols, targets, shots});

            req.getRequestDispatcher("game.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "Failed to parse game parameters: rows={0}, cols={1}, targets={2}, shots={3}",
                    new Object[]{rowsParam, colsParam, targetsParam, shotsParam});
            req.setAttribute("error", "Все параметры должны быть целыми числами");
            req.getRequestDispatcher("index.jsp").forward(req, resp);
        }
    }

    private String validateParams(int rows, int cols, int targets, int shots) {
        if (rows < 10 || rows > 20 || cols < 10 || cols > 20) {
            return "Размеры поля должны быть в диапазоне 10–20";
        }
        int maxTargets = (int) (Math.ceil(rows / 2.0) * Math.ceil(cols / 2.0));
        if (targets < 1 || targets > maxTargets) {
            return "Количество целей должно быть в диапазоне 1–⌈N/2⌉ × ⌈M/2⌉";
        }
        if (shots < targets || shots > rows * cols) {
            return "Количество выстрелов должно удовлетворять targets ≤ shots ≤ N × M";
        }
        return null;
    }
}


