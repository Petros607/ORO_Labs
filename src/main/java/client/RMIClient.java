package client;

import common.ShopService;
import model.Customer;
import model.Order;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RMIClient {

    private ShopService shopService;
    private Scanner scanner;

    public RMIClient() {
        this.scanner = new Scanner(System.in);
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            shopService = (ShopService) registry.lookup("ShopService");
            System.out.println("✅ Подключение к RMI серверу установлено");
        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к серверу: " + e.getMessage());
            System.exit(1);
        }
    }

    public void run() {
        connectToServer();

        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        displayAllCustomersWithOrders();
                        break;
                    case "2":
                        addNewOrder();
                        break;
                    case "3":
                        addNewCustomer();
                        break;
                    case "4":
                        deleteOrder();
                        break;
                    case "5":
                        deleteCustomer();
                        break;
                    case "6":
                        displayAllCustomers();
                        break;
                    case "7":
                        displayRecentOrders();
                        break;
                    case "0":
                        System.out.println("Выключение...");
                        return;
                    default:
                        System.out.println("❌ Неверный выбор");
                }
            } catch (Exception e) {
                System.out.println("❌ Ошибка: " + e.getMessage());
            }

//            System.out.println("\nНажмите Enter для продолжения...");
//            scanner.nextLine();
        }
    }

    private void printMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛒 RMI КЛИЕНТ МАГАЗИНА");
        System.out.println("=".repeat(60));
        System.out.println("1. Показать всех клиентов с заказами");
        System.out.println("2. Добавить новый заказ");
        System.out.println("3. Добавить нового клиента");
        System.out.println("4. Удалить заказ");
        System.out.println("5. Удалить клиента");
        System.out.println("6. Показать всех клиентов (кратко)");
        System.out.println("7. Показать последние заказы");
        System.out.println("0. Выход");
        System.out.print("\nВыберите действие: ");
    }

    private void displayAllCustomersWithOrders() throws Exception {
        System.out.println("\n📋 ПОЛУЧЕНИЕ ДАННЫХ С СЕРВЕРА...");

        Map<Customer, List<Order>> data = shopService.getAllCustomersWithOrders();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("КЛИЕНТЫ И ИХ ЗАКАЗЫ");
        System.out.println("=".repeat(80));

        for (Map.Entry<Customer, List<Order>> entry : data.entrySet()) {
            Customer customer = entry.getKey();
            List<Order> orders = entry.getValue();

            System.out.printf("\n👤 Клиент #%d: %s %s\n",
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName());
            System.out.printf("   Email: %s, Телефон: %s, Регистрация: %s\n",
                    customer.getEmail(),
                    customer.getPhone() == null ? "не указан" : customer.getPhone(),
                    customer.getRegistrationDate());

            if (orders.isEmpty()) {
                System.out.println("   ❌ Нет заказов");
            } else {
                System.out.println("   Заказы:");
                for (Order order : orders) {
                    System.out.printf("   • Заказ #%d | Дата: %s | Сумма: %.2f руб. | Статус: %s\n",
                            order.getOrderId(),
                            order.getOrderDate(),
                            order.getTotalAmount(),
                            order.getStatus());
                }
            }
        }
    }

    private void addNewOrder() throws Exception {
        System.out.println("\n📦 ДОБАВЛЕНИЕ НОВОГО ЗАКАЗА");

        displayAllCustomers();

        System.out.print("Введите ID клиента: ");
        int customerId = Integer.parseInt(scanner.nextLine());

        System.out.print("Введите сумму заказа: ");
        double amount = Double.parseDouble(scanner.nextLine());

        System.out.print("Статус (pending/shipped/completed/processing) [pending]: ");
        String status = scanner.nextLine();
        if (status.isEmpty()) status = "pending";

        Order newOrder = shopService.addOrder(customerId, amount, status);
        System.out.println("✅ Заказ успешно создан!");
        System.out.printf("   ID: %d, Сумма: %.2f, Статус: %s\n",
                newOrder.getOrderId(), newOrder.getTotalAmount(), newOrder.getStatus());
    }

    private void addNewCustomer() throws Exception {
        System.out.println("\n👤 ДОБАВЛЕНИЕ НОВОГО КЛИЕНТА");

        System.out.print("Имя: ");
        String firstName = scanner.nextLine();

        System.out.print("Фамилия: ");
        String lastName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Телефон (необязательно): ");
        String phone = scanner.nextLine();
        if (phone.isEmpty()) phone = null;

        Customer newCustomer = shopService.addCustomer(firstName, lastName, email, phone);
        System.out.println("✅ Клиент успешно создан!");
        System.out.printf("   ID: %d, %s %s, Email: %s\n",
                newCustomer.getCustomerId(),
                newCustomer.getFirstName(),
                newCustomer.getLastName(),
                newCustomer.getEmail());
    }

    private void deleteOrder() throws Exception {
        System.out.println("\n⚠️ УДАЛЕНИЕ ЗАКАЗА");

        displayRecentOrders();

        System.out.print("Введите ID заказа для удаления: ");
        int orderId = Integer.parseInt(scanner.nextLine());

        Order order = shopService.getOrderById(orderId);
        if (order == null) {
            System.out.println("❌ Заказ не найден");
            return;
        }

        System.out.println("\nИнформация о заказе:");
        System.out.printf("   Заказ #%d, Клиент: %s %s, Сумма: %.2f, Статус: %s\n",
                order.getOrderId(),
                order.getCustomerFirstName(),
                order.getCustomerLastName(),
                order.getTotalAmount(),
                order.getStatus());

//        System.out.print("Удалить клиента? (да/нет): ");
//        String confirm = scanner.nextLine().toLowerCase(); //TODO
        String confirm = "yes";

        if (confirm.equals("да") || confirm.equals("yes") || confirm.equals("y")) {
            boolean deleted = shopService.deleteOrder(orderId, true);
            if (deleted) {
                System.out.println("✅ Заказ успешно удален");
            } else {
                System.out.println("❌ Не удалось удалить заказ");
            }
        } else {
            System.out.println("❌ Удаление отменено");
        }
    }

    private void deleteCustomer() throws Exception {
        System.out.println("\n⚠️ УДАЛЕНИЕ КЛИЕНТА");

        displayAllCustomers();

        System.out.print("Введите ID клиента для удаления: ");
        int customerId = Integer.parseInt(scanner.nextLine());

        Customer customer = shopService.getCustomerById(customerId);
        if (customer == null) {
            System.out.println("❌ Клиент не найден");
            return;
        }

        System.out.println("\nИнформация о клиенте:");
        System.out.printf("   %s %s, Email: %s\n",
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail());

        Map<Customer, List<Order>> allData = shopService.getAllCustomersWithOrders();
        List<Order> customerOrders = null;
        for (Map.Entry<Customer, List<Order>> entry : allData.entrySet()) {
            if (entry.getKey().getCustomerId() == customerId) {
                customerOrders = entry.getValue();
                break;
            }
        }

        if (customerOrders != null && !customerOrders.isEmpty()) {
            System.out.println("\n⚠️ У клиента есть заказы:");
            for (Order order : customerOrders) {
                System.out.printf("   • Заказ #%d: %.2f руб.\n",
                        order.getOrderId(), order.getTotalAmount());
            }
            System.out.println("При удалении клиента все его заказы будут также удалены!");
        }

//        System.out.print("Удалить клиента? (да/нет): ");
//        String confirm = scanner.nextLine().toLowerCase(); //TODO
        String confirm = "yes";

        if (confirm.equals("да") || confirm.equals("yes") || confirm.equals("y")) {
            boolean deleted = shopService.deleteCustomer(customerId);
            if (deleted) {
                System.out.println("✅ Клиент успешно удален");
            } else {
                System.out.println("❌ Не удалось удалить клиента");
            }
        } else {
            System.out.println("❌ Удаление отменено");
        }
    }

    private void displayAllCustomers() throws Exception {
        List<Customer> customers = shopService.getAllCustomers();

        System.out.println("\n📋 КЛИЕНТЫ:");
        System.out.println("┌────┬───────────┬───────────┬──────────────────────────┐");
        System.out.println("│ ID │   Имя     │  Фамилия  │          Email           │");
        System.out.println("├────┼───────────┼───────────┼──────────────────────────┤");

        for (Customer c : customers) {
            System.out.printf("│ %-2d │ %-9s │ %-9s │ %-24s │%n",
                    c.getCustomerId(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getEmail());
        }
        System.out.println("└────┴───────────┴───────────┴──────────────────────────┘");
    }

    private void displayRecentOrders() throws Exception {
        List<Order> orders = shopService.getRecentOrders();

        System.out.println("\n📦 ПОСЛЕДНИЕ ЗАКАЗЫ:");
        System.out.println("┌────┬─────────────┬─────────────┬──────────┬─────────────────────┐");
        System.out.println("│ ID │   Клиент    │   Сумма     │  Статус  │       Дата          │");
        System.out.println("├────┼─────────────┼─────────────┼──────────┼─────────────────────┤");

        for (Order o : orders) {
            System.out.printf("│ %-2d │ %-11s │ %-11.2f │ %-8s │ %-19s │%n",
                    o.getOrderId(),
                    o.getCustomerFirstName() + " " + o.getCustomerLastName().charAt(0) + ".",
                    o.getTotalAmount(),
                    o.getStatus(),
                    o.getOrderDate().toString().substring(0, 19));
        }
        System.out.println("└────┴─────────────┴─────────────┴──────────┴─────────────────────┘");
    }

    public static void main(String[] args) {
        new RMIClient().run();
    }
}
