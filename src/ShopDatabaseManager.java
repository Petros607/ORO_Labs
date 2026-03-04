import java.sql.*;
import java.util.Scanner;

public class ShopDatabaseManager {
    private Connection connection;
    private Scanner scanner;

    private static final String URL = "jdbc:postgresql://localhost:5432/shop_db";
    private static final String USER = "marka";
    private static final String PASSWORD = "";

    public ShopDatabaseManager() {
        this.scanner = new Scanner(System.in);
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Подключение к БД установлено");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("❌ Ошибка подключения: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение закрыто");
            }
            scanner.close();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при закрытии: " + e.getMessage());
        }
    }

    // 1. Вывод всех строк первой таблицы с присоединением второй (эквисоединение)
    public void displayCustomersWithOrders() {
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone, " +
                "c.registration_date, o.order_id, o.order_date, o.total_amount, o.status " +
                "FROM customers c " +
                "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                "ORDER BY c.customer_id, o.order_date";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n" + "=".repeat(120));
            System.out.println("📋 КЛИЕНТЫ И ИХ ЗАКАЗЫ");
            System.out.println("=".repeat(120));

            int currentCustomerId = -1;
            boolean hasOrders = false;

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");

                // Если новый клиент
                if (customerId != currentCustomerId) {
                    currentCustomerId = customerId;
                    hasOrders = false;

                    System.out.printf("\n👤 Клиент #%d: %s %s\n",
                            customerId,
                            rs.getString("first_name"),
                            rs.getString("last_name"));
                    System.out.printf("   Email: %s, Телефон: %s, Регистрация: %s\n",
                            rs.getString("email"),
                            rs.getString("phone") == null ? "не указан" : rs.getString("phone"),
                            rs.getDate("registration_date"));
                    System.out.println("   " + "-".repeat(60));
                }

                // Проверяем, есть ли заказ
                int orderId = rs.getInt("order_id");
                if (orderId != 0) { // Если есть заказ (LEFT JOIN дает null для order_id)
                    hasOrders = true;
                    System.out.printf("   📦 Заказ #%d | Дата: %s | Сумма: %.2f руб. | Статус: %s\n",
                            orderId,
                            rs.getTimestamp("order_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"));
                } else if (!hasOrders) {
                    // Если это первая запись клиента и нет заказов
                    System.out.println("   ❌ Нет заказов");
                    hasOrders = true; // Чтобы не печатать несколько раз
                }
            }
            System.out.println("\n" + "=".repeat(120));

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при выводе данных: " + e.getMessage());
        }
    }

    // 2. Добавление записей во вторую таблицу (orders)
    public void addOrder() {
        System.out.println("\n📦 ДОБАВЛЕНИЕ НОВОГО ЗАКАЗА");

        displayCustomersSimple();

        try {
            System.out.print("Введите ID клиента: ");
            int customerId = Integer.parseInt(scanner.nextLine());

            if (!customerExists(customerId)) {
                System.out.println("❌ Клиент с ID " + customerId + " не существует");
                return;
            }

            System.out.print("Введите сумму заказа (руб.): ");
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Сумма должна быть положительной");
                return;
            }

            System.out.print("Введите статус (pending/shipped/completed/processing) [pending]: ");
            String status = scanner.nextLine();
            if (status.isEmpty()) {
                status = "pending";
            }

            if (!status.matches("pending|shipped|completed|processing")) {
                System.out.println("❌ Недопустимый статус. Используется 'pending'");
                status = "pending";
            }

            String sql = "INSERT INTO orders (customer_id, total_amount, status) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, customerId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, status);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        System.out.println("✅ Заказ #" + orderId + " успешно добавлен!");
                    }
                    generatedKeys.close();
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка ввода числа");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при добавлении заказа: " + e.getMessage());
        }
    }

    // 3. Добавление записей в первую таблицу (customers)
    public void addCustomer() {
        System.out.println("\n👤 ДОБАВЛЕНИЕ НОВОГО КЛИЕНТА");

        try {
            System.out.print("Введите имя: ");
            String firstName = scanner.nextLine();
            if (firstName.isEmpty()) {
                System.out.println("❌ Имя не может быть пустым");
                return;
            }

            System.out.print("Введите фамилию: ");
            String lastName = scanner.nextLine();
            if (lastName.isEmpty()) {
                System.out.println("❌ Фамилия не может быть пустой");
                return;
            }

            System.out.print("Введите email: ");
            String email = scanner.nextLine();
            if (email.isEmpty()) {
                System.out.println("❌ Email не может быть пустым");
                return;
            }

            System.out.print("Введите телефон (необязательно): ");
            String phone = scanner.nextLine();
            if (phone.isEmpty()) {
                phone = null;
            }

            String sql = "INSERT INTO customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        System.out.println("✅ Клиент #" + customerId + " успешно добавлен!");
                    }
                    generatedKeys.close();
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique constraint")) {
                System.out.println("❌ Клиент с таким email уже существует");
            } else {
                System.out.println("❌ Ошибка при добавлении клиента: " + e.getMessage());
            }
        }
    }

    // 4. Удаление записей из второй таблицы (orders) с проверкой связей
    public void deleteOrder() {
        System.out.println("\n⚠️ УДАЛЕНИЕ ЗАКАЗА");

        displayRecentOrders();

        try {
            System.out.print("Введите ID заказа для удаления: ");
            int orderId = Integer.parseInt(scanner.nextLine());

            if (!orderExists(orderId)) {
                System.out.println("❌ Заказ с ID " + orderId + " не существует");
                return;
            }

            String checkSql = "SELECT o.*, c.first_name, c.last_name FROM orders o " +
                    "JOIN customers c ON o.customer_id = c.customer_id " +
                    "WHERE o.order_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setInt(1, orderId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    System.out.println("\n📦 Информация о заказе:");
                    System.out.println("   Заказ #" + orderId);
                    System.out.println("   Клиент: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                    System.out.println("   Сумма: " + rs.getDouble("total_amount") + " руб.");
                    System.out.println("   Статус: " + rs.getString("status"));
                    System.out.println("   Дата: " + rs.getTimestamp("order_date"));

                    System.out.print("\n❓ Удалить этот заказ? (да/нет): ");
                    String confirm = scanner.nextLine().toLowerCase();
                    confirm = "yes"; //TODO

                    if (confirm.equals("да") || confirm.equals("yes") || confirm.equals("y")) {
                        // параметризованный запрос для удаления
                        String deleteSql = "DELETE FROM orders WHERE order_id = ?";
                        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, orderId);
                            int deleted = deleteStmt.executeUpdate();

                            if (deleted > 0) {
                                System.out.println("✅ Заказ #" + orderId + " успешно удален!");
                            }
                        }
                    } else {
                        System.out.println("❌ Удаление отменено");
                    }
                }
                rs.close();
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка ввода числа");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при удалении заказа: " + e.getMessage());
        }
    }

    // 5. Удаление записи из первой таблицы (customers) по первичному ключу
    public void deleteCustomer() {
        System.out.println("\n⚠️ УДАЛЕНИЕ КЛИЕНТА");

        displayCustomersSimple();

        try {
            System.out.print("Введите ID клиента для удаления: ");
            int customerId = Integer.parseInt(scanner.nextLine());

            if (!customerExists(customerId)) {
                System.out.println("❌ Клиент с ID " + customerId + " не существует");
                return;
            }

            String checkOrdersSql = "SELECT COUNT(*) FROM orders WHERE customer_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkOrdersSql)) {
                pstmt.setInt(1, customerId);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                int orderCount = rs.getInt(1);
                rs.close();

                String customerInfo = "SELECT first_name, last_name, email FROM customers WHERE customer_id = ?";
                try (PreparedStatement infoStmt = connection.prepareStatement(customerInfo)) {
                    infoStmt.setInt(1, customerId);
                    ResultSet infoRs = infoStmt.executeQuery();

                    if (infoRs.next()) {
                        System.out.println("\n👤 Информация о клиенте:");
                        System.out.println("   ID: " + customerId);
                        System.out.println("   Имя: " + infoRs.getString("first_name") + " " + infoRs.getString("last_name"));
                        System.out.println("   Email: " + infoRs.getString("email"));
                        System.out.println("   Всего заказов: " + orderCount);

                        if (orderCount > 0) {
                            System.out.println("\n⚠️ У клиента есть заказы! При удалении клиента все его заказы будут также удалены (CASCADE)");
                            displayCustomerOrders(customerId);
                        }

                        System.out.print("\n❓ Удалить клиента #" + customerId + "? (да/нет): ");
                        String confirm = scanner.nextLine().toLowerCase();
                        confirm = "yes"; //TODO

                        if (confirm.equals("да") || confirm.equals("yes") || confirm.equals("y")) {
                            // параметризованный запрос для удаления
                            String deleteSql = "DELETE FROM customers WHERE customer_id = ?";
                            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                                deleteStmt.setInt(1, customerId);
                                int deleted = deleteStmt.executeUpdate();

                                if (deleted > 0) {
                                    System.out.println("✅ Клиент #" + customerId + " успешно удален!");
                                    if (orderCount > 0) {
                                        System.out.println("   Также удалено " + orderCount + " заказов");
                                    }
                                }
                            }
                        } else {
                            System.out.println("❌ Удаление отменено");
                        }
                    }
                    infoRs.close();
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Ошибка ввода числа");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при удалении клиента: " + e.getMessage());
        }
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

    private void displayCustomersSimple() {
        String sql = "SELECT customer_id, first_name, last_name, email FROM customers ORDER BY customer_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📋 Существующие клиенты:");
            System.out.println("┌────┬───────────┬───────────┬──────────────────────────┐");
            System.out.println("│ ID │   Имя     │  Фамилия  │          Email           │");
            System.out.println("├────┼───────────┼───────────┼──────────────────────────┤");

            while (rs.next()) {
                System.out.printf("│ %-2d │ %-9s │ %-9s │ %-24s │%n",
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"));
            }
            System.out.println("└────┴───────────┴───────────┴──────────────────────────┘");

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при выводе клиентов: " + e.getMessage());
        }
    }

    private void displayRecentOrders() {
        String sql = "SELECT o.order_id, c.first_name, c.last_name, o.total_amount, o.status " +
                "FROM orders o JOIN customers c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_id DESC LIMIT 5";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n📦 Заказы:");
            System.out.println("┌────┬─────────────┬─────────────┬──────────┬─────────────┐");
            System.out.println("│ ID │   Клиент    │   Сумма     │  Статус  │             │");
            System.out.println("├────┼─────────────┼─────────────┼──────────┼─────────────┤");

            while (rs.next()) {
                System.out.printf("│ %-2d │ %-11s │ %-11.2f │ %-8s │%n",
                        rs.getInt("order_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name").charAt(0) + ".",
                        rs.getDouble("total_amount"),
                        rs.getString("status"));
            }
            System.out.println("└────┴─────────────┴─────────────┴──────────┴─────────────┘");

        } catch (SQLException e) {
            System.out.println("❌ Ошибка при выводе заказов: " + e.getMessage());
        }
    }

    private void displayCustomerOrders(int customerId) throws SQLException {
        String sql = "SELECT order_id, total_amount, status, order_date FROM orders WHERE customer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n   Заказы клиента:");
            while (rs.next()) {
                System.out.printf("   • Заказ #%d: %.2f руб. (%s) - %s\n",
                        rs.getInt("order_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date"));
            }
            rs.close();
        }
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🛒 МЕНЮ УПРАВЛЕНИЯ МАГАЗИНОМ");
            System.out.println("=".repeat(50));
            System.out.println("1. Показать всех клиентов с заказами");
            System.out.println("2. Добавить новый заказ");
            System.out.println("3. Добавить нового клиента");
            System.out.println("4. Удалить заказ");
            System.out.println("5. Удалить клиента");
            System.out.println("0. Выход");
            System.out.print("\nВыберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayCustomersWithOrders();
                    break;
                case "2":
                    addOrder();
                    break;
                case "3":
                    addCustomer();
                    break;
                case "4":
                    deleteOrder();
                    break;
                case "5":
                    deleteCustomer();
                    break;
                case "0":
                    System.out.println("Выход!");
                    return;
                default:
                    System.out.println("❌ Неверный выбор. Попробуйте снова.");
            }
        }
    }

    public static void main(String[] args) {
        ShopDatabaseManager manager = new ShopDatabaseManager();
        try {
            manager.showMenu();
        } finally {
            manager.close();
        }
    }
}
