package server;

import common.ShopService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer {

    public static void main(String[] args) {
        try {
            ShopServiceImpl shopService = new ShopServiceImpl();

            // экспорт удаленный объект (необязательно, UnicastRemoteObject)
            // ShopService stub = (ShopService) UnicastRemoteObject.exportObject(shopService, 0);

            // реестр RMI
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("✅ RMI реестр создан на порту 1099");

            registry.rebind("ShopService", shopService);
            System.out.println("✅ Сервис 'ShopService' зарегистрирован в реестре");

            System.out.println("\n🖥️ RMI Сервер запущен и готов к работе...");
            System.out.println("Нажмите Ctrl+C для остановки");

            synchronized (RMIServer.class) {
                RMIServer.class.wait();
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
