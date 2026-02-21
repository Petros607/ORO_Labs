import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class VectorServerSequential {

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Клиент подключен.");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);

            String input;

            while ((input = in.readLine()) != null) {

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Клиент завершил работу.");
                    break;
                }

                try {
                    double length = calculateVectorLength(input);

                    if (length == 0) {
                        out.println("Ошибка: длина вектора равна 0");
                    } else {
                        out.println("Длина вектора: " + length);
                    }

                } catch (Exception e) {
                    out.println("Ошибка ввода данных");
                }
            }

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double calculateVectorLength(String input) {
        String[] parts = input.split(" ");
        double sum = 0;

        for (String part : parts) {
            double value = Double.parseDouble(part);
            sum += value * value;
        }

        return Math.sqrt(sum);
    }
}
