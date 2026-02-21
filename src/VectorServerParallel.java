import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class VectorServerParallel {

    static void main(String[] args) {
        int port = 5001;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Параллельный сервер запущен.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключился новый клиент.");

                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            String input;

            while ((input = in.readLine()) != null) {

                if (input.equalsIgnoreCase("exit")) {
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

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateVectorLength(String input) {
        String[] parts = input.split(" ");
        double sum = 0;

        for (String part : parts) {
            double value = Double.parseDouble(part);
            sum += value * value;
        }

        return Math.sqrt(sum);
    }
}
