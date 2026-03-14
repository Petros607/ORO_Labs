package com.example.battleship.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtils {
    private static final Logger log = Logger.getLogger(DBUtils.class.getName());

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/seabattle";
    private static final String DB_USER = "marka";
    private static final String DB_PASSWORD = "";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "PostgreSQL JDBC Driver not found", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    public static User authenticate(String nickname, String rawPassword) {
        if (nickname == null || rawPassword == null) {
            return null;
        }
        String normalized = nickname.trim();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nickname, password, countOfWins FROM Users WHERE nickname = ?")) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (storedHash != null && storedHash.equals(hashPassword(rawPassword))) {
                        return new User(rs.getInt("id"), rs.getString("nickname"), rs.getInt("countOfWins"));
                    }
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error authenticating user", ex);
        }
        return null;
    }

    public static boolean nicknameExists(String nickname) {
        if (nickname == null) {
            return false;
        }
        String normalized = nickname.trim();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Users WHERE nickname = ?")) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error checking nickname existence", ex);
            return false;
        }
    }

    public static User register(String nickname, String rawPassword) {
        if (nickname == null || rawPassword == null) {
            return null;
        }
        String normalized = nickname.trim();
        if (normalized.isEmpty() || rawPassword.length() < 4) {
            return null;
        }

        if (nicknameExists(normalized)) {
            return null;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Users (nickname, password) VALUES (?, ?) RETURNING id, nickname, countOfWins")) {
            ps.setString(1, normalized);
            ps.setString(2, hashPassword(rawPassword));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("nickname"), rs.getInt("countOfWins"));
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error registering user", ex);
        }
        return null;
    }

    public static void incrementWins(String nickname) {
        if (nickname == null) {
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Users SET countOfWins = countOfWins + 1 WHERE nickname = ?")) {
            ps.setString(1, nickname);
            ps.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error incrementing user wins", ex);
        }
    }

    public static List<User> getTopPlayers(int limit) {
        List<User> result = new ArrayList<>();
        if (limit <= 0) {
            return result;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nickname, countOfWins FROM Users ORDER BY countOfWins DESC, nickname ASC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new User(rs.getInt("id"), rs.getString("nickname"), rs.getInt("countOfWins")));
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Error fetching top players", ex);
        }
        return result;
    }

    private static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
