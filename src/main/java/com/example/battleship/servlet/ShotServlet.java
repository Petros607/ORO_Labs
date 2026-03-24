package com.example.battleship.servlet;

import com.example.battleship.model.DBUtils;
import com.example.battleship.model.GameSession;
import com.example.battleship.model.ShotResult;
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

/**
 * Обработка выстрела по координатам.
 */
@WebServlet(name = "ShotServlet", urlPatterns = {"/shot"})
public class ShotServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ShotServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("game") == null) {
            log.log(Level.WARNING, "Shot request without active game session, remoteAddr={0}",
                    req.getRemoteAddr());
            req.setAttribute("error", "Игровая сессия не найдена. Начните новую игру.");
            req.getRequestDispatcher("index.jsp").forward(req, resp);
            return;
        }

        GameSession game = (GameSession) httpSession.getAttribute("game");

        String rowParam = req.getParameter("row");
        String colParam = req.getParameter("col");
        ShotResult result;

        try {
            int row = Integer.parseInt(rowParam) - 1;
            int col = Integer.parseInt(colParam) - 1;
            result = game.shoot(row, col);
            log.log(Level.INFO,
                    "Shot: sessionId={0}, row={1}, col={2}, valid={3}, hit={4}, status={5}, remainingShots={6}, destroyedTargets={7}",
                    new Object[]{
                            httpSession.getId(),
                            rowParam, colParam,
                            result.isValid(),
                            result.getHit(),
                            game.getStatus(),
                            game.getRemainingShots(),
                            game.getDestroyedTargets()
                    });
        } catch (NumberFormatException e) {
            log.log(Level.WARNING,
                    "Shot with invalid coordinates: sessionId={0}, row={1}, col={2}",
                    new Object[]{httpSession.getId(), rowParam, colParam});
            result = ShotResult.invalid("Координаты должны быть целыми числами");
        }

        req.setAttribute("shotResult", result);

        if (game.getStatus() == com.example.battleship.model.GameStatus.WON) {
            User currentUser = (User) httpSession.getAttribute("user");
            if (currentUser != null) {
                DBUtils.incrementWins(currentUser.getNickname());
                currentUser.setCountOfWins(currentUser.getCountOfWins() + 1);
                httpSession.setAttribute("user", currentUser);
            }
        }

        // Сессия уже обновлена внутри GameSession
        req.getRequestDispatcher("game.jsp").forward(req, resp);
    }
}
