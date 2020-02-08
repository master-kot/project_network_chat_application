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
    private final ChatServerListener listener;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    //список SocketThread от всех чат-клиентов
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
            //создание ServerSocketThread, слушатель - текуший экземпляр класса ChatServer
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
        SqlClient.connect();        //подключаемся к базе данных
        writeLog("SST Start");
    }

    @Override
    public void onServerSocketThreadStop(ServerSocketThread thread) {
        SqlClient.disconnect();     //отключаемся от базы данных
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        writeLog("SST Stop");
    }

    @Override
    public void onServerSocketCreate(ServerSocketThread thread, ServerSocket server) {
        writeLog("Server Socket created");
    }

    @Override
    public void onServerSocketAcceptTimeout(ServerSocketThread thread, ServerSocket server) {
        writeLog("Server socket accept timed out");
    }

    /**
     * Создание нового ClientThread как только произошел socket accept
     */
    @Override
    public void onSocketAccept(ServerSocketThread thread, Socket socket) {
        String threadName = "Socket thread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, threadName, socket); //новый клиентский поток
    }

    @Override
    public void onServerSocketThreadException(ServerSocketThread thread, Throwable throwable) {
        writeLog("Server socket thread exception: " + throwable.getMessage());
    }

    /**
     * События для слушателя, возникшие на Socket Thread
     */
    @Override
    public synchronized void onSocketThreadStart(SocketThread thread, Socket socket) {
        writeLog("SocketThread started");
    }

    /**
     * Если SocketThread остановился (один из клиентов вышел из чата)
     * Нужно уведомить всех пользователей об этом
     */
    @Override
    public synchronized void onSocketThreadStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnect()) {
            sendToAllAuthenticatedClients(Library.getTypeBroadcast("Server", client.getNickname() + " disconnected"));
            sendToAllAuthenticatedClients(Library.getUserList(getUsers()));
        }
    }

    @Override
    public synchronized void onSocketThreadReady(SocketThread thread, Socket socket) {
        clients.add(thread);
    }

    /**
     * Получение сообщение от авторизованных и неавторизованных клиентов
     * @param thread - сокет поток для конкретного пользователя
     */
    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthMessage(client, msg);
        } else {
            handleNonAuthMessage(client, msg);  //если клиент не авторизован
        }
    }

    @Override
    public synchronized void onSocketThreadException(SocketThread thread, Throwable throwable) {
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

    /**
     * Получено сообщение от авторизованного клиента
     */
    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BCAST_CLIENT:     //всем авторизованным клиентам
                sendToAllAuthenticatedClients(msg);
                break;
            case Library.TYPE_BROADCAST:        //
                //TODO
                //sendToAllAuthenticatedClients(msg);
                break;
            case Library.MSG_FORMAT_ERROR:      //ответ на сообщение клиента об ошибке
                client.sendMessage(Library.getMsgFormatError(msg));
                break;
            default:
                /* если принято сообщение неизвестного формата:
                 * throw new RuntimeException("You are trying to hack me! " + msg);
                 * Не лучшее решение падать с исключением, лучше сообщить клиенту
                 */
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    /**
     * Получено сообщение от неавторизованного клиента, оно должно
     * содержать только данные пользователя для их проверки по базе данных
     * и дальнейшей авторизации. Иные сообщения в чат отправляться не будут
     * @param newClient - новый клиент, пока еще не авторизованный
     */
    private void handleNonAuthMessage(ClientThread newClient, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        /* Строка авторизации должна быть вида: auth_request±login±password
         * если строка не содержит эти данные (количество больше или меньше)
         * выдаем ошибку формата сообщения авторизации
         */
        //TODO
        //Сделать обработку запроса авторизации внутри библиотеки
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            newClient.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            writeLog(String.format("Invalid login attempt: l='%s', p='%s'", login, password));
            newClient.authFail();
            return;
        }
        /*
         * Если клиент уже залогинился, но хочет войти в чат с другого экземпляра
         * программы например с телефона или другого ПК
         */
        ClientThread oldClient = findClientByNick(nickname);
        newClient.authAccept(nickname);
        if (oldClient == null) {
            sendToAllAuthenticatedClients(Library.getTypeBroadcast("Server", nickname + " connected!"));
        } else {
            oldClient.reconnect();      //закрываем соединение старого клиента
            clients.remove(oldClient);  //удаляем старого клиента из списка
        }
        //Уведомляем всех клиентов, что новый пользователь вошел в чат
        sendToAllAuthenticatedClients(Library.getUserList(getUsers()));
    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if(!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private ClientThread findClientByNick(String nick) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if(!client.isAuthorized()) continue;
            if (client.getNickname().equals(nick))
                return client;
        }
        return null;
    }
}