import java.io.IOException;
import java.net.Socket;

public class Client implements Runnable {
        protected Connection connection;
        private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    @Override
    public void run() {
        Thread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Error");
                return;
            }
        }
        if (clientConnected) ConsoleHelper.writeMessage("Соединение установлено.");
        else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (clientConnected) {
        String kekw = ConsoleHelper.readString();
        if (kekw.equals("exit")) break;
        else if (shouldSendTextFromConsole()) sendTextMessage(kekw);
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " joined");
        }
        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " left");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
                Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    Message kekw = new Message(MessageType.USER_NAME,getUserName());
                    connection.send(kekw);
                } else
                if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
            Message message = connection.receive();
            if (message.getType() == MessageType.TEXT) processIncomingMessage(message.getData());
            else if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
            else if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
            else throw new IOException("Unexpected MessageType"); }
        }
        public void run () {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                System.out.println("5");
                Socket socket = new Socket(address,port);
                System.out.println("4");
                connection = new Connection(socket);
                System.out.println("1");
                clientHandshake();
                System.out.println("2");
                clientMainLoop();
                System.out.println("3");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("SHIT");
                notifyConnectionStatusChanged(false);
            }
        }
    }
    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }
    protected int getServerPort()  {
        return ConsoleHelper.readInt();
    }
    protected String getUserName ()  {
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole() {
        return true;
    }
    protected SocketThread getSocketThread () {
        return new SocketThread();
    }
    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error");
            clientConnected = false;
        }
    }

}
