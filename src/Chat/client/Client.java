package Chat.client;
import Chat.Connection;
import Chat.ConsoleHelper;
import Chat.Message;
import Chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;


    protected String getServerAddress() {
        try {
            return ConsoleHelper.readString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected int getServerPort() {
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        try {
            return ConsoleHelper.readString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {

            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);

        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка при отправке");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {

            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("возникла ошибка");
            return;
        }

        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }


        while (clientConnected) {
            String date;
            try {
                date = ConsoleHelper.readString();
                if (date.equals("exit")) break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(date);
            }
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    public class SocketThread extends Thread {

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                MessageType messageType = message.getType();
                String data = message.getData();
                if (messageType == MessageType.TEXT) processIncomingMessage(data);
                else if (messageType == MessageType.USER_ADDED) informAboutAddingNewUser(data);
                else if (messageType == MessageType.USER_REMOVED) informAboutDeletingNewUser(data);
                else throw new IOException("Unexpected MessageType");
            }
        }


        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }


        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }


        @Override
        public void run() {
            try (Socket socket = new Socket(getServerAddress(), getServerPort()))
            {
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }


        }
    }
}
