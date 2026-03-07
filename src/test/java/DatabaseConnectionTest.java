import java.sql.*;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/shop_db";
        String user = "marka";
        String password = "";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Драйвер загружен!");

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Соединение с БД установлено!");

            rs = conn.createStatement().executeQuery("SELECT version()");
            if (rs.next()) {
                System.out.println("ℹ️ PostgreSQL version: " + rs.getString(1));
            }
            rs.close();

            stmt = conn.createStatement();
            String sql = "SELECT customer_id, first_name, last_name, email, registration_date FROM customers";
            rs = stmt.executeQuery(sql);

            System.out.println("\n📋 Список клиентов:");
            System.out.println("┌────┬───────────┬───────────┬──────────────────────────┬──────────────┐");
            System.out.println("│ ID │   Имя     │  Фамилия  │          Email           │ Дата рег.    │");
            System.out.println("├────┼───────────┼───────────┼──────────────────────────┼──────────────┤");

            while (rs.next()) {
                int id = rs.getInt("customer_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                Date regDate = rs.getDate("registration_date");

                System.out.printf("│ %-2d │ %-9s │ %-9s │ %-24s │ %-12s │%n",
                        id, firstName, lastName, email, regDate);
            }
            System.out.println("└────┴───────────┴───────────┴──────────────────────────┴──────────────┘");

            rs.close();
            rs = stmt.executeQuery(
                    "SELECT COUNT(*) as order_count, " +
                            "COUNT(DISTINCT customer_id) as customers_with_orders " +
                            "FROM orders"
            );

            if (rs.next()) {
                System.out.println("\n📊 Статистика:");
                System.out.println("   Всего заказов: " + rs.getInt("order_count"));
                System.out.println("   Клиентов с заказами: " + rs.getInt("customers_with_orders"));
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Драйвер PostgreSQL не найден!");
            System.out.println("   Добавьте JAR-файл через: File → Project Structure → Libraries");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при работе с БД!");
            System.out.println("   SQL State: " + e.getSQLState());
            System.out.println("   Error Code: " + e.getErrorCode());
            System.out.println("   Message: " + e.getMessage());

            System.out.println("\n🔧 Проверьте:");
            System.out.println("1. Запущен ли PostgreSQL: pg_ctl status");
            System.out.println("2. Существует ли БД shop_db: \\l в psql");
            System.out.println("3. Права доступа: GRANT ALL ON DATABASE shop_db TO marka;");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
