package com.example;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class GameServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("start".equals(action)) {
            int min = Integer.parseInt(request.getParameter("min"));
            int max = Integer.parseInt(request.getParameter("max"));
            if (min >= max) {
                throw new IllegalArgumentException("Min must be less than max");
            }
            session.setAttribute("min", min);
            session.setAttribute("max", max);
            int guess = (min + max) / 2;
            session.setAttribute("guess", guess);
            request.getRequestDispatcher("/game.jsp").forward(request, response);
        } else if ("guess".equals(action)) {
            String responseParam = request.getParameter("response");
            int min = (Integer) session.getAttribute("min");
            int max = (Integer) session.getAttribute("max");
            int guess = (Integer) session.getAttribute("guess");

            if ("higher".equals(responseParam)) {
                min = guess + 1;
            } else if ("lower".equals(responseParam)) {
                max = guess - 1;
            } else if ("equal".equals(responseParam)) {
                request.getRequestDispatcher("/win.jsp").forward(request, response);
                return;
            }

            if (min > max) {
                request.getRequestDispatcher("/cheat.jsp").forward(request, response);
                return;
            }

            guess = (min + max) / 2;
            session.setAttribute("min", min);
            session.setAttribute("max", max);
            session.setAttribute("guess", guess);
            request.getRequestDispatcher("/game.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}