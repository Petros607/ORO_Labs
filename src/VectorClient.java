import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class VectorClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true);
                Scanner scanner = new Scanner(System.in)
        ) {

            System.out.println("Подключено к серверу.");
            System.out.println("Введите координаты через пробел (или exit для выхода)");

            while (true) {
                System.out.print("Вектор: ");
                String input = scanner.nextLine();

                out.println(input);

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                String response = in.readLine();
                System.out.println("Ответ сервера: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
