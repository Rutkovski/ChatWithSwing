package Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//основной класс сервера.
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap();

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Не смогли отправить сообщение");
            }
        }

    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            ConsoleHelper.writeMessage("Было установленно новое соединение с удаленным адресом: " + socket.getRemoteSocketAddress());
            String username = null;
            try (Connection connection = new Connection(socket)) {
                username = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, username));
                notifyUsers(connection, username);
                serverMainLoop(connection, username);


            } catch (Exception e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
            if (username != null) {
                connectionMap.remove(username);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, username));
            }
            ConsoleHelper.writeMessage("Соединение с удаленным сервером закрыто");

        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                String userName = message.getData();
                if ((message.getType() == MessageType.USER_NAME) &&
                        !userName.isEmpty() &&
                        !connectionMap.containsKey(userName)) {
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    connectionMap.put(userName, connection);
                    return userName;
                }
            }

        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            connectionMap.forEach((key, value) -> {
                        if (key != userName) {
                            try {
                                connection.send(new Message(MessageType.USER_ADDED, key));
                            } catch (IOException e) {
                            }
                        }
                    }
            );
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String string = String.format("%s: %s", userName, message.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, string));
                } else {
                    ConsoleHelper.writeMessage("Ошибка");
                }
            }

        }

    }


    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
