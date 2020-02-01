package chat.server.core;

import chat.library.Library;
import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Класс передает слушателю события, полученные от Server Socket и Socket потоков
 */
public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private ServerSocketThread server;
    private ChatServerListener listener;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    /**
     * Запуск сервер-сокет потока на определенном порту
     */
    public void start(int port) {
        if (server != null && server.isAlive()) {
            writeLog("Server is already running");
        } else {
            server = new ServerSocketThread(this, "Server", port, 2000);
            writeLog("Server thread started at port: " + port);
        }
    }

    /**
     * Остановка сервер-сокет потока
     */
    public void stop() {
        if (server == null || !server.isAlive()) {
            writeLog("Server is not running!");
        } else {
            server.interrupt();
            writeLog("Server interrupted");
        }
    }

    /**
     * Логируем возникающие события, отдавая их слушателю
     */
    private void writeLog(String msg) {
        msg = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() +
                ": " + msg;
        listener.onServerMessage(msg);
    }

    /**
     * События для слушателя Server Socket Thread
     **/
    @Override
    public void onServerSocketThreadStart(ServerSocketThread thread) {
        SqlClient.connect();
        writeLog("SST Start");
    }

    @Override
    public void onServerSocketThreadStop(ServerSocketThread thread) {
        SqlClient.disconnect();
        writeLog("SST Stop");
    }

    @Override
    public void onServerSocketCreate(ServerSocketThread thread, ServerSocket server) {
        writeLog("Server Socket created");
    }

    @Override
    public void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server) {
//        writeLog("Server socket accept timed out");
    }

    @Override
    public void onSocketAccept(ServerSocketThread thread, Socket socket) {
        String threadName = "Socket thread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, threadName, socket);
    }

    @Override
    public void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable) {
        writeLog("Server socket thread exception: " + throwable.getMessage());
    }

    /**
     * События для слушателя, возникшие на Socket Thread
     * */
    @Override
    public void onSocketThreadStart(SocketThread thread, Socket socket) {
        writeLog("SocketThread started");
    }

    @Override
    public void onSocketThreadStop(SocketThread thread) {
        clients.remove(thread);
    }

    @Override
    public void onSocketThreadReady(SocketThread thread, Socket socket) {
        clients.add(thread);
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMessage(client, msg);
        } else {
            handleNonAuthMessage(client, msg);
        }
    }

    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        writeLog("SocketThread Exception: " + throwable.getMessage());
        clients.remove(thread);
    }

    private void sendToAllAuthenticatedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        sendToAllAuthenticatedClients(msg);
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            writeLog(String.format("Invalid login attempt: l='%s', p='%s'", login, password));
            client.authFail();
            return;
        }
        client.authAccept(nickname);
        sendToAllAuthenticatedClients(Library.getTypeBroadcast("Server", nickname + " connected!"));
    }
}