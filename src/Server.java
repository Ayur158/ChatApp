import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static class Handler extends Thread {
        private Socket socket;
        public Handler (Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST));
            Message name = connection.receive();
            if (name.getType() != MessageType.USER_NAME || name.getData().isEmpty() || connectionMap.containsKey(name.getData())) serverHandshake(connection);
            else {
                connectionMap.put(name.getData(),connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
            }
            return connection.receive().getData();
        }
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                Message comand = new Message(MessageType.USER_ADDED,entry.getKey());
                if (entry.getKey() != userName && entry.getValue() != connection )connection.send(comand);
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
            Message kekw = connection.receive();
            String mesData = "";
            if (kekw.getType() == MessageType.TEXT) {
                mesData = userName + ": " + kekw.getData();
                sendBroadcastMessage(new Message(MessageType.TEXT,mesData));
            } else ConsoleHelper.writeMessage("ERROR"); }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("New connection" + socket.getRemoteSocketAddress());
            try
            {   Connection connection = new Connection(socket);
                String name = serverHandshake(connection);
                notifyUsers(connection, name);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                serverMainLoop(connection,name);
                connectionMap.remove(name, connection);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                if (name!= null) connectionMap.remove(name);
            } catch (Exception e) {
                ConsoleHelper.writeMessage("ERRORUS");
            }
        }
    }

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String,Connection> entry : connectionMap.entrySet()) {
            try {
            entry.getValue().send(message);
            } catch (Exception e) {
                System.out.println("We could'nt send message :(");
            }
        }
    }
    public static void main(String[] args) throws IOException {
        System.out.println("Enter number of port");
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = new ServerSocket(port);
        ConsoleHelper.writeMessage("Started");
        while (true) {
            try
            {
                Socket kekw = serverSocket.accept();
            Handler handler = new Handler(kekw);
            handler.start();}
            catch (Exception ex) {
                serverSocket.close();
                System.out.println(ex);
                break;
            }
        }
    }
}
