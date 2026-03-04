package server;

import common.ShopService;
import model.Customer;
import model.Order;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация удаленного сервиса
 **/
public class ShopServiceImpl extends UnicastRemoteObject implements ShopService {

    private Connection connection;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/shop_db";
    private static final String DB_USER = "marka";
    private static final String DB_PASSWORD = "";

    public ShopServiceImpl() throws RemoteException {
        super();
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Сервер: подключение к БД установлено");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Сервер: ошибка подключения к БД: " + e.getMessage());
        }
    }

    @Override
    public Map<Customer, List<Order>> getAllCustomersWithOrders() throws RemoteException {
        Map<Customer, List<Order>> result = new HashMap<>();

        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone, " +
                "c.registration_date, o.order_id, o.order_date, o.total_amount, o.status " +
                "FROM customers c " +
                "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                "ORDER BY c.customer_id, o.order_date";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Customer currentCustomer = null;
            List<Order> currentOrders = null;

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");

                // Если новый клиент
                if (currentCustomer == null || currentCustomer.getCustomerId() != customerId) {
                    if (currentCustomer != null) {
                        result.put(currentCustomer, currentOrders);
                    }

                    currentCustomer = new Customer(
                            customerId,
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getDate("registration_date")
                    );
                    currentOrders = new ArrayList<>();
                }

                int orderId = rs.getInt("order_id");
                if (orderId != 0) {
                    Order order = new Order(
                            orderId,
                            customerId,
                            rs.getTimestamp("order_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("status")
                    );
                    currentOrders.add(order);
                }
            }

            if (currentCustomer != null) {
                result.put(currentCustomer, currentOrders);
            }

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при получении данных: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Order addOrder(int customerId, double amount, String status)
            throws RemoteException, IllegalArgumentException, SQLException {

        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }

        if (status == null || status.isEmpty()) {
            status = "pending";
        }

        if (!status.matches("pending|shipped|completed|processing")) {
            throw new IllegalArgumentException("Недопустимый статус заказа");
        }

        // Проверяем существование клиента
        if (!customerExists(customerId)) {
            throw new IllegalArgumentException("Клиент с ID " + customerId + " не существует");
        }

        String sql = "INSERT INTO orders (customer_id, total_amount, status) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, customerId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, status);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    rs.close();

                    // Получаем полную информацию о созданном заказе
                    return getOrderById(orderId);
                }
                rs.close();
            }

            throw new RemoteException("Не удалось создать заказ");

        } catch (SQLException e) {
            throw new RemoteException("Ошибка БД: " + e.getMessage());
        }
    }

    @Override
    public Customer addCustomer(String firstName, String lastName, String email, String phone)
            throws RemoteException, IllegalArgumentException {

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Фамилия не может быть пустой");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        String sql = "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int customerId = rs.getInt(1);
                    rs.close();

                    // Получаем полную информацию о созданном клиенте
                    return getCustomerById(customerId);
                }
                rs.close();
            }

            throw new RemoteException("Не удалось создать клиента");

        } catch (SQLException e) {
            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("duplicate key")) {
                throw new IllegalArgumentException("Клиент с таким email уже существует");
            }
            throw new RemoteException("Ошибка БД: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteOrder(int orderId, boolean force) throws RemoteException, SQLException {
        if (!orderExists(orderId)) {
            return false;
        }

        // Получаем информацию о заказе и связанном клиенте
        String checkSql = "SELECT customer_id FROM orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String deleteSql = "DELETE FROM orders WHERE order_id = ?";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, orderId);
                    int deleted = deleteStmt.executeUpdate();
                    return deleted > 0;
                }
            }
            rs.close();

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при удалении заказа: " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean deleteCustomer(int customerId) throws RemoteException, SQLException {
        if (!customerExists(customerId)) {
            return false;
        }

        // Удаляем клиента (каскадно)
        String deleteSql = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setInt(1, customerId);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RemoteException("Ошибка при удалении клиента: " + e.getMessage());
        }
    }

    @Override
    public List<Customer> getAllCustomers() throws RemoteException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("registration_date")
                ));
            }

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при получении клиентов: " + e.getMessage());
        }

        return customers;
    }

    @Override
    public List<Order> getRecentOrders() throws RemoteException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.first_name, c.last_name " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_id DESC LIMIT 10";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                );
                order.setCustomerFirstName(rs.getString("first_name"));
                order.setCustomerLastName(rs.getString("last_name"));
                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при получении заказов: " + e.getMessage());
        }

        return orders;
    }

    @Override
    public Customer getCustomerById(int customerId) throws RemoteException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDate("registration_date")
                );
                rs.close();
                return customer;
            }
            rs.close();

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при получении клиента: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Order getOrderById(int orderId) throws RemoteException {
        String sql = "SELECT o.*, c.first_name, c.last_name " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE o.order_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                );
                order.setCustomerFirstName(rs.getString("first_name"));
                order.setCustomerLastName(rs.getString("last_name"));
                rs.close();
                return order;
            }
            rs.close();

        } catch (SQLException e) {
            throw new RemoteException("Ошибка при получении заказа: " + e.getMessage());
        }

        return null;
    }

    private boolean customerExists(int customerId) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;
        }
    }

    private boolean orderExists(int orderId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            return exists;
        }
    }
}
