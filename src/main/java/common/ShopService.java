package common;

import model.Customer;
import model.Order;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Remote интерфейс для работы с магазином
 */
public interface ShopService extends Remote {

    // 1. Получить всех клиентов с их заказами
    Map<Customer, List<Order>> getAllCustomersWithOrders() throws RemoteException;

    // 2. Добавить новый заказ
    Order addOrder(int customerId, double amount, String status) throws RemoteException, IllegalArgumentException, SQLException;

    // 3. Добавить нового клиента
    Customer addCustomer(String firstName, String lastName, String email, String phone)
            throws RemoteException, IllegalArgumentException;

    // 4. Удалить заказ (с проверкой)
    boolean deleteOrder(int orderId, boolean force) throws RemoteException, SQLException;

    // 5. Удалить клиента
    boolean deleteCustomer(int customerId) throws RemoteException, SQLException;

    List<Customer> getAllCustomers() throws RemoteException;
    List<Order> getRecentOrders() throws RemoteException;
    Customer getCustomerById(int customerId) throws RemoteException;
    Order getOrderById(int orderId) throws RemoteException;
}
